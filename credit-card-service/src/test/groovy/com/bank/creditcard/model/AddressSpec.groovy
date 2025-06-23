package com.bank.creditcard.model

import spock.lang.Specification

class AddressSpec extends Specification {

    def "should create address with all fields"() {
        when: "creating an address with all fields"
        def address = new Address(
                streetAddress: "123 Main St",
                city: "New York",
                state: "NY",
                zipCode: "10001",
                country: "USA"
        )
        
        then: "all fields are set correctly"
        address.streetAddress == "123 Main St"
        address.city == "New York"
        address.state == "NY"
        address.zipCode == "10001"
        address.country == "USA"
    }

    def "should create address with no-args constructor"() {
        when: "creating an address with no-args constructor"
        def address = new Address()
        
        then: "address is created with null fields"
        address != null
        address.streetAddress == null
        address.city == null
        address.state == null
        address.zipCode == null
        address.country == null
    }

    def "should handle equals and hashCode correctly"() {
        given: "two addresses"
        def address1 = new Address("123 Main St", "New York", "NY", "10001", "USA")
        def address2 = new Address("123 Main St", "New York", "NY", "10001", "USA")
        def address3 = new Address("456 Oak Ave", "Los Angeles", "CA", "90001", "USA")
        
        expect: "equal addresses have same hashCode"
        address1 == address2
        address1.hashCode() == address2.hashCode()
        
        and: "different addresses are not equal"
        address1 != address3
        address1.hashCode() != address3.hashCode()
    }

    def "should handle null values in equals"() {
        given: "addresses with null values"
        def address1 = new Address(null, "New York", "NY", "10001", "USA")
        def address2 = new Address(null, "New York", "NY", "10001", "USA")
        def address3 = new Address("123 Main St", "New York", "NY", "10001", "USA")
        
        expect: "addresses with same null values are equal"
        address1 == address2
        
        and: "address with null is not equal to address with value"
        address1 != address3
    }

    def "should provide meaningful toString"() {
        given: "an address"
        def address = new Address(
                streetAddress: "123 Main St",
                city: "New York",
                state: "NY",
                zipCode: "10001",
                country: "USA"
        )
        
        when: "calling toString"
        def result = address.toString()
        
        then: "string contains all address components"
        result.contains("123 Main St")
        result.contains("New York")
        result.contains("NY")
        result.contains("10001")
        result.contains("USA")
    }

    def "should handle partial addresses"() {
        given: "addresses with missing fields"
        def addressNoZip = new Address("123 Main St", "New York", "NY", null, "USA")
        def addressNoCountry = new Address("123 Main St", "New York", "NY", "10001", null)
        def addressMinimal = new Address(null, "New York", "NY", null, null)
        
        expect: "partial addresses are valid"
        addressNoZip.city == "New York"
        addressNoZip.zipCode == null
        
        addressNoCountry.zipCode == "10001"
        addressNoCountry.country == null
        
        addressMinimal.streetAddress == null
        addressMinimal.city == "New York"
        addressMinimal.state == "NY"
    }

    def "should be embeddable in JPA entities"() {
        given: "an address used as embedded object"
        def address = new Address(
                streetAddress: "789 Park Blvd",
                city: "San Francisco",
                state: "CA",
                zipCode: "94102",
                country: "USA"
        )
        
        expect: "address has @Embeddable annotation"
        Address.class.isAnnotationPresent(jakarta.persistence.Embeddable.class)
        
        and: "fields have @Column annotations"
        Address.class.getDeclaredField("streetAddress").isAnnotationPresent(jakarta.persistence.Column.class)
        Address.class.getDeclaredField("city").isAnnotationPresent(jakarta.persistence.Column.class)
        Address.class.getDeclaredField("state").isAnnotationPresent(jakarta.persistence.Column.class)
        Address.class.getDeclaredField("zipCode").isAnnotationPresent(jakarta.persistence.Column.class)
        Address.class.getDeclaredField("country").isAnnotationPresent(jakarta.persistence.Column.class)
    }

    def "should support different address formats"() {
        given: "various address formats"
        def usAddress = new Address("123 Main St", "New York", "NY", "10001", "USA")
        def intlAddress = new Address("10 Downing Street", "London", "England", "SW1A 2AA", "UK")
        def aptAddress = new Address("123 Main St Apt 4B", "New York", "NY", "10001-1234", "USA")
        
        expect: "all formats are supported"
        usAddress.state == "NY"
        usAddress.zipCode == "10001"
        
        intlAddress.city == "London"
        intlAddress.zipCode == "SW1A 2AA"
        
        aptAddress.streetAddress == "123 Main St Apt 4B"
        aptAddress.zipCode == "10001-1234"
    }

    def "should handle address updates"() {
        given: "an existing address"
        def address = new Address("123 Main St", "New York", "NY", "10001", "USA")
        
        when: "updating address fields"
        address.streetAddress = "456 Updated St"
        address.city = "Brooklyn"
        address.zipCode = "11201"
        
        then: "fields are updated"
        address.streetAddress == "456 Updated St"
        address.city == "Brooklyn"
        address.state == "NY"
        address.zipCode == "11201"
        address.country == "USA"
    }
}
