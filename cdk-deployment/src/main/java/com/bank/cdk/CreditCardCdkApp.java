package com.bank.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import com.bank.cdk.stack.CreditCardServiceStack;

public class CreditCardCdkApp {

    public static void main(final String[] args) {
        App app = new App();

        // Define the environment
        Environment env = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv("CDK_DEFAULT_REGION"))
                .build();

        // Create the stack
        new CreditCardServiceStack(app, "CreditCardServiceStack", StackProps.builder()
                .env(env)
                .description("Credit Card Application Service Infrastructure")
                .build());

        app.synth();
    }
}
