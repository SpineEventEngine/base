package io.spine.validate;

/**
 * A test suite covering the {@link ValidatingBuilder} behavior.
 *
 * <p>Since most {@code ValidatingBuilders} are generated, the concrete test suits for each of them
 * are not required.
 *
 * <p>Any {@code ValidatingBuilder} implementation should pass these tests. When implementing your
 * own {@code ValidatingBuilder}, be sure to check if it fits the constraints stated below.
 *
 * @author Dmytro Dashenkov
 */
public class ValidatingBuilderShould {

//    private CustomerVBuilder builder;
//
//    @Before
//    public void setUp() {
//        builder = fill();
//    }
//
//    @Test(expected = ValidationException.class)
//    public void check_required_validated_repeated_fields() {
//        builder.addName("invalid_name");
//    }
//
//    @Test(expected = ValidationException.class)
//    public void ensure_required_validated_repeated_fields() {
//        builder.clearName();
//        builder.build();
//    }
//
//    @Test(expected = ValidationException.class)
//    public void check_required_validated_map_field_keys() {
//        builder.putPositionInCompany("", PersonName.newBuilder()
//                                                   .setGivenName("Abraham")
//                                                   .setFamilyName("Lincoln")
//                                                   .build());
//    }
//
//    @Test(expected = ValidationException.class)
//    public void check_required_validated_map_field_values() {
//        builder.putPositionInCompany("President of the US", PersonName.getDefaultInstance());
//    }
//
//    @Test(expected = ValidationException.class)
//    public void ensure_required_validated_map_fields() {
//        builder.clearPositionInCompany();
//        builder.build();
//    }
//
//    @Test(expected = ValidationException.class)
//    public void check_validated_repeated_fields() {
//        builder.addEmail(EmailAddress.getDefaultInstance());
//    }
//
//    @Test
//    public void dispense_with_validated_repeated_fields() {
//        builder.clearEmail();
//        builder.build();
//    }
//
//    @Test(expected = ValidationException.class)
//    public void check_validated_map_field_keys() {
//        final Timestamp timeInPast = add(getCurrentTime(), fromSeconds(-10L));
//        builder.putContract("", Contract.newBuilder()
//                                        .setSince(timeInPast)
//                                        .setText("My contract text")
//                                        .build());
//    }
//
//    @Test(expected = ValidationException.class)
//    public void check_validated_map_field_values() {
//        builder.putContract(Identifier.newUuid(), Contract.getDefaultInstance());
//    }
//
//    @Test
//    public void dispense_with_validated_map_fields() {
//        builder.clearContract();
//        builder.build();
//    }
//
//    @Test
//    public void accept_any_required_repeated_fields() {
//        builder.addProduct(Product.UNRECOGNIZED);
//    }
//
//    @Test(expected = ValidationException.class)
//    public void ensure_required_repeated_fields() {
//        builder.clearProduct();
//        builder.build();
//    }
//
//    @Test
//    public void accept_any_required_map_field_key() {
//        builder.putAppearedOnMarket("", Time.getCurrentTime());
//    }
//
//    @Test
//    public void accept_any_required_map_field_value() {
//        builder.putAppearedOnMarket("Montenegro", Timestamp.getDefaultInstance());
//    }
//
//    @Test(expected = ValidationException.class)
//    public void ensure_required_map_fields() {
//        builder.clearAppearedOnMarket();
//        builder.build();
//    }
//
//    @Test
//    public void accept_any_unchecked_repeated_fields() {
//        builder.addFaxNumber("Who knows how do they actually look like?");
//    }
//
//    @Test
//    public void dispense_with_unchecked_repeated_fields() {
//        builder.clearFaxNumber();
//        builder.build();
//    }
//
//    @Test
//    public void accept_any_unchecked_map_field_key() {
//        final Timestamp timeInPast = add(getCurrentTime(), fromSeconds(-1000L));
//        builder.putOldContract("", Contract.newBuilder()
//                                           .setSince(timeInPast)
//                                           .setText("My very old contract text")
//                                           .build());
//    }
//
//    @Test
//    public void accept_any_unchecked_map_field_value() {
//        builder.putOldContract("Forgotten", Contract.getDefaultInstance());
//    }
//
//    @Test
//    public void dispense_with_unchecked_map_fields() {
//        builder.clearOldContract();
//        builder.build();
//    }
//
//    /**
//     * Creates a valid {@link CustomerVBuilder} instance.
//     */
//    private static CustomerVBuilder fill() {
//        final CustomerVBuilder builder = CustomerVBuilder.newBuilder();
//        builder.build(); // Ensure no ValidationException is thrown.
//        return builder;
//    }
}
