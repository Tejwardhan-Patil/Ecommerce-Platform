import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import ApiService from '../services/ApiService';
import { useCart } from '../hooks/useCart';
import './ProductDetailPage.scss';

const ProductDetailPage = () => {
  const { productId } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState('');
  const { addToCart } = useCart();

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const data = await ApiService.getProductDetails(productId);
        setProduct(data);
      } catch (error) {
        setError('Failed to fetch product details.');
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [productId]);

  const handleAddToCart = () => {
    if (quantity > product.stock) {
      setError('Quantity exceeds stock available');
      return;
    }
    addToCart(product, quantity);
  };

  if (loading) {
    return <div className="product-detail-page">Loading...</div>;
  }

  if (error) {
    return <div className="product-detail-page">{error}</div>;
  }

  return (
    <div className="product-detail-page">
      <div className="product-detail-container">
        <div className="product-image">
          <img src={product.imageUrl} alt={product.name} />
        </div>
        <div className="product-info">
          <h1>{product.name}</h1>
          <p>{product.description}</p>
          <p className="product-price">${product.price.toFixed(2)}</p>
          <p className="product-stock">{product.stock} in stock</p>

          <div className="quantity-control">
            <label htmlFor="quantity">Quantity:</label>
            <input
              type="number"
              id="quantity"
              name="quantity"
              min="1"
              max={product.stock}
              value={quantity}
              onChange={(e) => setQuantity(parseInt(e.target.value))}
            />
          </div>

          <button
            className="add-to-cart-button"
            onClick={handleAddToCart}
            disabled={quantity < 1 || quantity > product.stock}
          >
            Add to Cart
          </button>
        </div>
      </div>

      <div className="product-reviews">
        <h2>Customer Reviews</h2>
        {product.reviews && product.reviews.length > 0 ? (
          <ul>
            {product.reviews.map((review, index) => (
              <li key={index} className="review-item">
                <h3>{review.title}</h3>
                <p>{review.body}</p>
                <div className="review-rating">
                  Rating: {review.rating} / 5
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p>No reviews yet.</p>
        )}
      </div>

      <div className="related-products">
        <h2>Related Products</h2>
        <div className="related-product-list">
          {product.relatedProducts.map((relatedProduct) => (
            <div key={relatedProduct.id} className="related-product-card">
              <img
                src={relatedProduct.imageUrl}
                alt={relatedProduct.name}
                className="related-product-image"
              />
              <div className="related-product-info">
                <h3>{relatedProduct.name}</h3>
                <p className="related-product-price">
                  ${relatedProduct.price.toFixed(2)}
                </p>
                <button
                  className="view-product-button"
                  onClick={() =>
                    window.location.href = `/products/${relatedProduct.id}`
                  }
                >
                  View Product
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;