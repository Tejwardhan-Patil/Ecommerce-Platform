package core.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique Product ID.
 */
public final class ProductID implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private final String id;

    /**
     * Private constructor for ProductID.
     * Use static factory methods to create instances.
     *
     * @param id the unique identifier for the product.
     */
    private ProductID(String id) {
        validate(id);
        this.id = id;
    }

    /**
     * Validates the given product ID string.
     *
     * @param id The product ID to validate.
     * @throws IllegalArgumentException if the product ID is invalid.
     */
    private void validate(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty.");
        }
        if (id.length() != 36) { // UUID is 36 characters long.
            throw new IllegalArgumentException("Product ID must be a valid UUID.");
        }
    }

    /**
     * Creates a new ProductID from a string.
     *
     * @param id The product ID string.
     * @return A new ProductID instance.
     */
    public static ProductID of(String id) {
        return new ProductID(id);
    }

    /**
     * Generates a new random ProductID.
     *
     * @return A new ProductID with a random UUID.
     */
    public static ProductID generate() {
        return new ProductID(UUID.randomUUID().toString());
    }

    /**
     * Gets the string representation of the product ID.
     *
     * @return The product ID as a string.
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductID productID = (ProductID) o;
        return Objects.equals(id, productID.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductID{" +
                "id='" + id + '\'' +
                '}';
    }
    
    /**
     * Returns true if the product ID matches the given ID.
     *
     * @param otherId The ID to compare against.
     * @return true if the IDs match, false otherwise.
     */
    public boolean matches(ProductID otherId) {
        return this.id.equals(otherId.id);
    }

    /**
     * Checks if the ProductID is valid.
     *
     * @param id The product ID string.
     * @return true if the ID is valid, false otherwise.
     */
    public static boolean isValid(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(id);
            return id.length() == 36;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * A builder for ProductID that supports fluent creation.
     */
    public static class Builder {
        private String id;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public ProductID build() {
            return new ProductID(id);
        }
    }
    
    /**
     * Converts ProductID to a DTO representation.
     *
     * @return A new ProductDTO with the product ID.
     */
    public ProductDTO toDTO() {
        return new ProductDTO(this.id);
    }

    /**
     * Constructs a ProductID from a DTO representation.
     *
     * @param dto The ProductDTO.
     * @return A ProductID instance.
     */
    public static ProductID fromDTO(ProductDTO dto) {
        return new ProductID(dto.getId());
    }

    /**
     * Ensures immutability during serialization and deserialization.
     *
     * @return The original ProductID object.
     */
    private Object readResolve() {
        return of(this.id);
    }
}

/**
 * DTO class for ProductID.
 */
class ProductDTO {
    private String id;

    public ProductDTO(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

/**
 * A utility class to work with ProductID in collections.
 */
class ProductIDCollection {

    private List<ProductID> productIDs;

    public ProductIDCollection() {
        this.productIDs = new ArrayList<>();
    }

    public void addProductID(ProductID productID) {
        productIDs.add(productID);
    }

    public boolean contains(ProductID productID) {
        return productIDs.contains(productID);
    }

    public void removeProductID(ProductID productID) {
        productIDs.remove(productID);
    }

    public List<ProductID> getAllProductIDs() {
        return Collections.unmodifiableList(productIDs);
    }
    
    public int size() {
        return productIDs.size();
    }
}

/**
 * A utility class for generating random ProductID collections.
 */
class ProductIDGenerator {

    public static List<ProductID> generateRandomCollection(int size) {
        List<ProductID> productIDs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            productIDs.add(ProductID.generate());
        }
        return productIDs;
    }

    public static void main(String[] args) {
        List<ProductID> randomIDs = ProductIDGenerator.generateRandomCollection(5);
        randomIDs.forEach(System.out::println);
    }
}