import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import './OrderSummary.scss';
import { formatCurrency } from '../utils/currencyFormatter';
import { calculateTax, calculateDiscount } from '../services/OrderService';
import { getCartItems } from '../services/ApiService';

const OrderSummary = ({ userId }) => {
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [subtotal, setSubtotal] = useState(0);
  const [tax, setTax] = useState(0);
  const [discount, setDiscount] = useState(0);
  const [total, setTotal] = useState(0);
  const [shippingCost, setShippingCost] = useState(5.99); 
  const [promoCode, setPromoCode] = useState('');
  const [isPromoValid, setIsPromoValid] = useState(false);

  useEffect(() => {
    const fetchCartItems = async () => {
      setLoading(true);
      try {
        const items = await getCartItems(userId);
        setCartItems(items);
        const newSubtotal = calculateSubtotal(items);
        setSubtotal(newSubtotal);
        const newTax = calculateTax(newSubtotal);
        setTax(newTax);
        const newDiscount = calculateDiscount(newSubtotal, promoCode);
        setDiscount(newDiscount);
        const newTotal = newSubtotal + newTax + shippingCost - newDiscount;
        setTotal(newTotal);
      } catch (error) {
        console.error('Error fetching cart items:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchCartItems();
  }, [userId, promoCode]);

  const handleApplyPromoCode = () => {
    if (promoCode.trim().toLowerCase() === 'discount10') {
      setIsPromoValid(true);
    } else {
      setIsPromoValid(false);
    }
  };

  const calculateSubtotal = (items) => {
    return items.reduce((acc, item) => acc + item.price * item.quantity, 0);
  };

  if (loading) {
    return <div className="loading">Loading your order summary...</div>;
  }

  return (
    <div className="order-summary-container">
      <h2>Order Summary</h2>
      <div className="order-summary-content">
        <div className="order-summary-items">
          {cartItems.map((item) => (
            <div key={item.id} className="order-summary-item">
              <div className="item-name">{item.name}</div>
              <div className="item-price">{formatCurrency(item.price)}</div>
              <div className="item-quantity">Quantity: {item.quantity}</div>
              <div className="item-total">{formatCurrency(item.price * item.quantity)}</div>
            </div>
          ))}
        </div>

        <div className="order-summary-details">
          <div className="summary-detail">
            <span>Subtotal:</span>
            <span>{formatCurrency(subtotal)}</span>
          </div>
          <div className="summary-detail">
            <span>Shipping:</span>
            <span>{formatCurrency(shippingCost)}</span>
          </div>
          <div className="summary-detail">
            <span>Tax:</span>
            <span>{formatCurrency(tax)}</span>
          </div>
          <div className="summary-detail">
            <span>Discount:</span>
            <span>{formatCurrency(discount)}</span>
          </div>
          <div className="summary-detail">
            <span>Total:</span>
            <span className="total-amount">{formatCurrency(total)}</span>
          </div>
        </div>

        <div className="promo-code-section">
          <input
            type="text"
            value={promoCode}
            onChange={(e) => setPromoCode(e.target.value)}
            placeholder="Enter promo code"
          />
          <button onClick={handleApplyPromoCode}>Apply</button>
          {isPromoValid ? (
            <div className="promo-valid-message">Promo code applied!</div>
          ) : promoCode ? (
            <div className="promo-invalid-message">Invalid promo code</div>
          ) : null}
        </div>
      </div>
    </div>
  );
};

OrderSummary.propTypes = {
  userId: PropTypes.string.isRequired,
};

export default OrderSummary;