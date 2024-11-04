package com.ecommerce.app.models

import com.ecommerce.app.utils.CurrencyUtils
import java.util.Date

data class Product(
    val id: String,
    var name: String,
    var description: String,
    var price: Double,
    var discount: Double = 0.0,
    var stockQuantity: Int,
    var category: ProductCategory,
    var brand: String,
    var images: List<String> = emptyList(),
    var reviews: List<Review> = emptyList(),
    var createdAt: Date = Date(),
    var updatedAt: Date = Date(),
    var isAvailable: Boolean = true
) {

    init {
        validateFields()
    }

    // Calculate the final price after applying the discount
    fun getFinalPrice(): Double {
        return if (discount > 0) {
            price - (price * discount / 100)
        } else {
            price
        }
    }

    // Check if the product is in stock
    fun isInStock(): Boolean {
        return stockQuantity > 0
    }

    // Add stock to the product
    fun addStock(quantity: Int) {
        if (quantity > 0) {
            stockQuantity += quantity
            updatedAt = Date()
        }
    }

    // Deduct stock from the product, ensuring stock does not go below zero
    fun reduceStock(quantity: Int) {
        if (quantity > 0 && stockQuantity >= quantity) {
            stockQuantity -= quantity
            updatedAt = Date()
        } else {
            throw IllegalArgumentException("Not enough stock available")
        }
    }

    // Toggle product availability status
    fun toggleAvailability() {
        isAvailable = !isAvailable
        updatedAt = Date()
    }

    // Format price to display in the user's preferred currency
    fun formattedPrice(currency: String): String {
        return CurrencyUtils.formatCurrency(getFinalPrice(), currency)
    }

    // Add a review for the product
    fun addReview(review: Review) {
        reviews = reviews + review
        updatedAt = Date()
    }

    // Calculate the average rating from the reviews
    fun getAverageRating(): Double {
        return if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average()
        } else {
            0.0
        }
    }

    // Check if the product has a discount
    fun hasDiscount(): Boolean {
        return discount > 0
    }

    // Validate product fields upon creation
    private fun validateFields() {
        if (name.isBlank()) throw IllegalArgumentException("Product name cannot be blank")
        if (description.isBlank()) throw IllegalArgumentException("Product description cannot be blank")
        if (price < 0) throw IllegalArgumentException("Product price cannot be negative")
        if (stockQuantity < 0) throw IllegalArgumentException("Stock quantity cannot be negative")
    }

    // Update the product details
    fun updateDetails(
        newName: String?,
        newDescription: String?,
        newPrice: Double?,
        newDiscount: Double?,
        newStockQuantity: Int?,
        newImages: List<String>?
    ) {
        newName?.let { name = it }
        newDescription?.let { description = it }
        newPrice?.let { price = it }
        newDiscount?.let { discount = it }
        newStockQuantity?.let { stockQuantity = it }
        newImages?.let { images = it }
        updatedAt = Date()
    }
}

data class ProductCategory(
    val id: String,
    val name: String
)

data class Review(
    val id: String,
    val userId: String,
    val rating: Double,
    val comment: String,
    val createdAt: Date = Date()
)