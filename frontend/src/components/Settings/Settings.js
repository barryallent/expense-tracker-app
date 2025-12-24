import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import './Settings.css';

const Settings = () => {
  const { user, currency, axiosInstance } = useAuth();
  const [selectedCurrency, setSelectedCurrency] = useState(currency || 'USD');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const currencies = [
    { code: 'USD', name: 'US Dollar', symbol: '$' },
    { code: 'EUR', name: 'Euro', symbol: '€' },
    { code: 'GBP', name: 'British Pound', symbol: '£' },
    { code: 'INR', name: 'Indian Rupee', symbol: '₹' },
    { code: 'JPY', name: 'Japanese Yen', symbol: '¥' },
    { code: 'CAD', name: 'Canadian Dollar', symbol: 'C$' },
    { code: 'AUD', name: 'Australian Dollar', symbol: 'A$' },
    { code: 'CHF', name: 'Swiss Franc', symbol: 'CHF' },
    { code: 'CNY', name: 'Chinese Yuan', symbol: '¥' },
    { code: 'SEK', name: 'Swedish Krona', symbol: 'kr' },
  ];

  const handleSave = async () => {
    try {
      setError('');
      setMessage('');
      
      await axiosInstance.put('/users/currency', { currency: selectedCurrency });
      
      setMessage('Currency updated successfully! Please refresh the page.');
      
      // Reload page after 2 seconds to apply changes
      setTimeout(() => {
        window.location.reload();
      }, 2000);
    } catch (err) {
      setError('Failed to update currency');
      console.error(err);
    }
  };

  return (
    <div className="settings-container">
      <div className="settings-header">
        <h1>Settings</h1>
        <p>Manage your account preferences</p>
      </div>

      <div className="settings-content">
        <div className="settings-section">
          <h2>Account Information</h2>
          <div className="info-grid">
            <div className="info-item">
              <label>Username</label>
              <p>{user?.username}</p>
            </div>
            <div className="info-item">
              <label>Email</label>
              <p>{user?.email}</p>
            </div>
            <div className="info-item">
              <label>Full Name</label>
              <p>{user?.fullName}</p>
            </div>
          </div>
        </div>

        <div className="settings-section">
          <h2>Currency Preference</h2>
          <p className="section-description">
            Select your preferred currency for displaying amounts
          </p>
          
          <div className="currency-selector">
            <label htmlFor="currency">Currency</label>
            <select 
              id="currency"
              value={selectedCurrency}
              onChange={(e) => setSelectedCurrency(e.target.value)}
              className="currency-select"
            >
              {currencies.map((curr) => (
                <option key={curr.code} value={curr.code}>
                  {curr.symbol} {curr.name} ({curr.code})
                </option>
              ))}
            </select>
          </div>

          {message && (
            <div className="success-message">
              {message}
            </div>
          )}

          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <button 
            className="save-btn"
            onClick={handleSave}
            disabled={selectedCurrency === currency}
          >
            Save Changes
          </button>
        </div>
      </div>
    </div>
  );
};

export default Settings;

