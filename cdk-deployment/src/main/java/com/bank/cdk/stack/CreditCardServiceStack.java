package com.bank.cdk.stack;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.*;
import software.amazon.awscdk.services.logs.*;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.Map;

public class CreditCardServiceStack extends Stack {

    public CreditCardServiceStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CreditCardServiceStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create VPC
        Vpc vpc = Vpc.Builder.create(this, "CreditCardVpc")
                .maxAzs(2)
                .natGateways(1)
                .build();

        // Create database credentials secret
        software.amazon.awscdk.services.secretsmanager.Secret dbSecret = 
                software.amazon.awscdk.services.secretsmanager.Secret.Builder.create(this, "DbSecret")
                .description("RDS Database Credentials")
                .generateSecretString(software.amazon.awscdk.services.secretsmanager.SecretStringGenerator.builder()
                        .secretStringTemplate("{\"username\": \"creditcard_user\"}")
                        .generateStringKey("password")
                        .excludeCharacters(" %+~`#$&*()|[]{}:;<>?!'/\\")
                        .build())
                .build();

        // Create RDS PostgreSQL instance
        DatabaseInstance database = DatabaseInstance.Builder.create(this, "CreditCardDb")
                .engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_15)
                        .build()))
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .instanceType(software.amazon.awscdk.services.ec2.InstanceType.of(InstanceClass.T3, InstanceSize.MICRO))
                .allocatedStorage(20)
                .databaseName("creditcard_db")
                .credentials(Credentials.fromSecret(dbSecret))
                .multiAz(false)
                .deletionProtection(false)
                .build();

        // Create ECS Cluster
        Cluster cluster = Cluster.Builder.create(this, "CreditCardCluster")
                .vpc(vpc)
                .build();

        // Create Fargate Service
        ApplicationLoadBalancedFargateService fargateService = 
                ApplicationLoadBalancedFargateService.Builder.create(this, "CreditCardService")
                        .cluster(cluster)
                        .cpu(512)
                        .memoryLimitMiB(1024)
                        .desiredCount(2)
                        .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromAsset("../credit-card-service"))
                                .containerPort(8080)
                                .environment(Map.of(
                                        "SPRING_PROFILES_ACTIVE", "aws",
                                        "SERVER_PORT", "8080"
                                ))
                                .secrets(Map.of(
                                        "SPRING_DATASOURCE_PASSWORD", 
                                        software.amazon.awscdk.services.ecs.Secret.fromSecretsManager(dbSecret, "password")
                                ))
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .streamPrefix("credit-card-service")
                                        .logRetention(RetentionDays.ONE_WEEK)
                                        .build()))
                                .build())
                        .publicLoadBalancer(true)
                        .build();

        // Allow Fargate service to connect to RDS
        database.getConnections().allowFrom(
                fargateService.getService(),
                Port.tcp(5432),
                "Allow Fargate to RDS"
        );

        // Configure health check
        fargateService.getTargetGroup().configureHealthCheck(software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck.builder()
                .path("/actuator/health")
                .build());

        // Output the ALB URL
        this.exportValue(fargateService.getLoadBalancer().getLoadBalancerDnsName());
    }
}
