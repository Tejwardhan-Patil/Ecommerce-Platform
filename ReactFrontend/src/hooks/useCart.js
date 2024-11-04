import { useState, useEffect, useCallback } from 'react';
import ApiService from '../services/ApiService';

const useCart = () => {
  const [cartItems, setCartItems] = useState([]);
  const [totalAmount, setTotalAmount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const savedCart = JSON.parse(localStorage.getItem('cart'));
    if (savedCart) {
      setCartItems(savedCart);
    }
  }, []);

  useEffect(() => {
    calculateTotal();
  }, [cartItems]);

  const saveCartToLocalStorage = useCallback((items) => {
    localStorage.setItem('cart', JSON.stringify(items));
  }, []);

  const calculateTotal = useCallback(() => {
    const total = cartItems.reduce((acc, item) => acc + item.price * item.quantity, 0);
    setTotalAmount(total);
  }, [cartItems]);

  const addToCart = useCallback((product, quantity = 1) => {
    setLoading(true);
    const existingProduct = cartItems.find(item => item.id === product.id);
    if (existingProduct) {
      const updatedCart = cartItems.map(item =>
        item.id === product.id ? { ...item, quantity: item.quantity + quantity } : item
      );
      setCartItems(updatedCart);
      saveCartToLocalStorage(updatedCart);
    } else {
      const updatedCart = [...cartItems, { ...product, quantity }];
      setCartItems(updatedCart);
      saveCartToLocalStorage(updatedCart);
    }
    setLoading(false);
  }, [cartItems, saveCartToLocalStorage]);

  const updateQuantity = useCallback((productId, quantity) => {
    setLoading(true);
    const updatedCart = cartItems.map(item =>
      item.id === productId ? { ...item, quantity } : item
    );
    setCartItems(updatedCart);
    saveCartToLocalStorage(updatedCart);
    setLoading(false);
  }, [cartItems, saveCartToLocalStorage]);

  const removeFromCart = useCallback((productId) => {
    setLoading(true);
    const updatedCart = cartItems.filter(item => item.id !== productId);
    setCartItems(updatedCart);
    saveCartToLocalStorage(updatedCart);
    setLoading(false);
  }, [cartItems, saveCartToLocalStorage]);

  const clearCart = useCallback(() => {
    setLoading(true);
    setCartItems([]);
    saveCartToLocalStorage([]);
    setLoading(false);
  }, [saveCartToLocalStorage]);

  const fetchCartDetails = useCallback(async () => {
    setLoading(true);
    try {
      const response = await ApiService.getCart();
      setCartItems(response.data);
      saveCartToLocalStorage(response.data);
    } catch (err) {
      setError('Failed to load cart');
    } finally {
      setLoading(false);
    }
  }, [saveCartToLocalStorage]);

  const checkout = useCallback(async () => {
    setLoading(true);
    try {
      const response = await ApiService.checkout(cartItems);
      if (response.status === 200) {
        clearCart();
      }
    } catch (err) {
      setError('Checkout failed');
    } finally {
      setLoading(false);
    }
  }, [cartItems, clearCart]);

  const isItemInCart = useCallback((productId) => {
    return cartItems.some(item => item.id === productId);
  }, [cartItems]);

  return {
    cartItems,
    totalAmount,
    loading,
    error,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    fetchCartDetails,
    checkout,
    isItemInCart,
  };
};

export default useCart;