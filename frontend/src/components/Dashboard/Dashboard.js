import React, { useState, useEffect, useCallback } from 'react';
import { TrendingUp, TrendingDown, Wallet, Calendar, ArrowUpRight, ArrowDownRight, Plus, BarChart3 } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const Dashboard = () => {
  const { axiosInstance, currency, user } = useAuth();
  const navigate = useNavigate();
  const [summary, setSummary] = useState({
    income: 0,
    expense: 0,
    balance: 0,
    transactionCount: 0
  });
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchDashboardData = useCallback(async () => {
    try {
      setLoading(true);
      const [summaryResponse, transactionsResponse] = await Promise.all([
        axiosInstance.get('/transactions/summary'),
        axiosInstance.get('/transactions')
      ]);
      
      setSummary({
        income: summaryResponse.data.income || 0,
        expense: summaryResponse.data.expense || 0,
        balance: summaryResponse.data.balance || 0,
        transactionCount: transactionsResponse.data.length || 0
      });
      
      // Get last 5 transactions
      setRecentTransactions(transactionsResponse.data.slice(0, 5));
      setError('');
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  }, [axiosInstance]);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(Math.abs(amount));
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric'
    });
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 18) return 'Good Afternoon';
    return 'Good Evening';
  };

  const savingsRate = summary.income > 0 ? ((summary.balance / summary.income) * 100).toFixed(1) : 0;

  if (loading) {
    return (
      <div className="dashboard">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading your financial overview...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div>
          <h1>{getGreeting()}, {user?.fullName?.split(' ')[0] || 'there'}! 👋</h1>
          <p className="subtitle">Here's your financial snapshot for {new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}</p>
        </div>
        <button className="add-transaction-btn" onClick={() => navigate('/transactions')}>
          <Plus size={20} />
          Add Transaction
        </button>
      </div>

      {error && (
        <div className="error-banner">
          <span>{error}</span>
        </div>
      )}

      <div className="stats-grid">
        <div className="stat-card income-card">
          <div className="stat-header">
            <div className="stat-icon income-icon">
              <TrendingUp size={24} />
            </div>
            <span className="stat-label">Total Income</span>
          </div>
          <div className="stat-body">
            <h2 className="stat-amount">{formatCurrency(summary.income)}</h2>
            <p className="stat-subtitle">This month</p>
          </div>
        </div>

        <div className="stat-card expense-card">
          <div className="stat-header">
            <div className="stat-icon expense-icon">
              <TrendingDown size={24} />
            </div>
            <span className="stat-label">Total Expenses</span>
          </div>
          <div className="stat-body">
            <h2 className="stat-amount">{formatCurrency(summary.expense)}</h2>
            <p className="stat-subtitle">This month</p>
          </div>
        </div>

        <div className="stat-card balance-card">
          <div className="stat-header">
            <div className="stat-icon balance-icon">
              <Wallet size={24} />
            </div>
            <span className="stat-label">Net Balance</span>
          </div>
          <div className="stat-body">
            <h2 className={`stat-amount ${summary.balance >= 0 ? 'positive' : 'negative'}`}>
              {summary.balance >= 0 ? '+' : ''}{formatCurrency(summary.balance)}
            </h2>
            <p className="stat-subtitle">Savings: {savingsRate}%</p>
          </div>
        </div>

        <div className="stat-card transactions-card">
          <div className="stat-header">
            <div className="stat-icon transactions-icon">
              <Calendar size={24} />
            </div>
            <span className="stat-label">Transactions</span>
          </div>
          <div className="stat-body">
            <h2 className="stat-amount">{summary.transactionCount}</h2>
            <p className="stat-subtitle">This month</p>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="recent-transactions-card">
          <div className="card-header">
            <h3>Recent Transactions</h3>
            <button className="view-all-btn" onClick={() => navigate('/transactions')}>
              View All
            </button>
          </div>
          <div className="transactions-list">
            {recentTransactions.length > 0 ? (
              recentTransactions.map((transaction) => (
                <div key={transaction.id} className="transaction-item">
                  <div className="transaction-icon-wrapper">
                    <div className={`transaction-icon ${transaction.type.toLowerCase()}`}>
                      {transaction.type === 'INCOME' ? (
                        <ArrowDownRight size={18} />
                      ) : (
                        <ArrowUpRight size={18} />
                      )}
                    </div>
                  </div>
                  <div className="transaction-details">
                    <p className="transaction-description">{transaction.description}</p>
                    <span className="transaction-category">{transaction.category?.name}</span>
                  </div>
                  <div className="transaction-amount-wrapper">
                    <p className={`transaction-amount ${transaction.type.toLowerCase()}`}>
                      {transaction.type === 'INCOME' ? '+' : '-'}{formatCurrency(transaction.amount)}
                    </p>
                    <span className="transaction-date">{formatDate(transaction.transactionDate)}</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <Calendar size={48} />
                <p>No transactions yet</p>
                <button className="empty-action-btn" onClick={() => navigate('/transactions')}>
                  <Plus size={18} />
                  Add your first transaction
                </button>
              </div>
            )}
          </div>
        </div>

        <div className="quick-actions-card">
          <h3>Quick Actions</h3>
          <div className="actions-grid">
            <button className="action-card" onClick={() => navigate('/transactions')}>
              <div className="action-icon income-bg">
                <Plus size={24} />
              </div>
              <div className="action-content">
                <h4>Add Transaction</h4>
                <p>Record income or expense</p>
              </div>
            </button>
            <button className="action-card" onClick={() => navigate('/reports')}>
              <div className="action-icon expense-bg">
                <BarChart3 size={24} />
              </div>
              <div className="action-content">
                <h4>View Reports</h4>
                <p>Analyze your spending</p>
              </div>
            </button>
            <button className="action-card" onClick={() => navigate('/categories')}>
              <div className="action-icon balance-bg">
                <Calendar size={24} />
              </div>
              <div className="action-content">
                <h4>Categories</h4>
                <p>Manage categories</p>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
