package bet.ahh.architect.examples;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import bet.ahh.architect.Architect;

public class Product {
    private final String id;
    private final String name;
    private final double price;
    private final String category;
    private final boolean active;
    private final Optional<String> description;

    public Product(String id, String name, double price) {
        this(id, name, price, null, null, Optional.empty());
    }

    @Architect
    public Product(
            String id,
            String name,
            double price,
            @Nullable String category,
            @Nullable Boolean active,
            Optional<String> description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category != null ? category : "General";
        this.active = active != null ? active : true;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public Optional<String> getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", active=" + active +
                ", description=" + description +
                '}';
    }
}
