// ProductCard.js
import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { addToCart, addToWishlist } from '../store/reduxStore';
import '../styles/main.scss';

const ProductCard = ({ product }) => {
  const [isHovered, setIsHovered] = useState(false);
  const dispatch = useDispatch();

  const handleMouseEnter = () => {
    setIsHovered(true);
  };

  const handleMouseLeave = () => {
    setIsHovered(false);
  };

  const handleAddToCart = () => {
    dispatch(addToCart(product));
  };

  const handleAddToWishlist = () => {
    dispatch(addToWishlist(product));
  };

  return (
    <div
      className={`product-card ${isHovered ? 'hovered' : ''}`}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <Link to={`/product/${product.id}`} className="product-card-link">
        <div className="product-card-image-wrapper">
          <img
            src={product.imageUrl}
            alt={product.name}
            className="product-card-image"
          />
        </div>
        <div className="product-card-details">
          <h3 className="product-card-title">{product.name}</h3>
          <p className="product-card-description">
            {product.description.length > 100
              ? `${product.description.substring(0, 100)}...`
              : product.description}
          </p>
          <div className="product-card-price-rating">
            <span className="product-card-price">${product.price}</span>
            <span className="product-card-rating">{product.rating} ★</span>
          </div>
        </div>
      </Link>
      <div className="product-card-actions">
        <button className="btn btn-primary" onClick={handleAddToCart}>
          Add to Cart
        </button>
        <button className="btn btn-secondary" onClick={handleAddToWishlist}>
          Add to Wishlist
        </button>
      </div>
    </div>
  );
};

export default ProductCard;