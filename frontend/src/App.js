import React, { useEffect, useState } from 'react';
import { getAccount, getHoldings, runRebalance } from './api/client';
import AllocationBar from './components/AllocationBar';
import TradeTable from './components/TradeTable';

// Hardcoded for v1 - swap for a client/account picker once auth exists
const DEMO_ACCOUNT_ID = 1;

function App() {
  const [account, setAccount] = useState(null);
  const [holdings, setHoldings] = useState([]);
  const [rebalanceResult, setRebalanceResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    getAccount(DEMO_ACCOUNT_ID).then((res) => setAccount(res.data)).catch(() => {});
    getHoldings(DEMO_ACCOUNT_ID).then((res) => setHoldings(res.data)).catch(() => {});
  }, []);

  const handleRebalance = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await runRebalance(DEMO_ACCOUNT_ID);
      setRebalanceResult(res.data);
    } catch (err) {
      setError('Failed to run rebalance. Is the backend running on port 8080?');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-shell">
      <h1>Portfolio Rebalancing Dashboard</h1>

      <div className="card">
        <h2>Account Overview</h2>
        {account ? (
          <p>
            Account #{account.id} — {account.accountType} — Cash: $
            {Number(account.cashBalance).toLocaleString()}
          </p>
        ) : (
          <p>Loading account...</p>
        )}

        <h3>Current Holdings</h3>
        <table>
          <thead>
            <tr>
              <th>Ticker</th>
              <th>Quantity</th>
              <th>Price</th>
              <th>Value</th>
            </tr>
          </thead>
          <tbody>
            {holdings.map((h) => (
              <tr key={h.id}>
                <td>{h.security.ticker}</td>
                <td>{h.quantity}</td>
                <td>${Number(h.security.currentPrice).toFixed(2)}</td>
                <td>${(h.quantity * h.security.currentPrice).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card">
        <h2>Rebalance</h2>
        <button onClick={handleRebalance} disabled={loading}>
          {loading ? 'Calculating...' : 'Run Rebalance'}
        </button>
        {error && <p style={{ color: '#dc2626' }}>{error}</p>}

        {rebalanceResult && (
          <div style={{ marginTop: 20 }}>
            <p>Total Portfolio Value: ${Number(rebalanceResult.totalPortfolioValue).toLocaleString()}</p>

            <AllocationBar label="Current Allocation" weights={rebalanceResult.currentAllocation} />
            <AllocationBar label="Target Allocation" weights={rebalanceResult.targetAllocation} />
            <AllocationBar label="Projected Allocation (after trades)" weights={rebalanceResult.projectedAllocation} />

            <h3>Proposed Trades</h3>
            <TradeTable trades={rebalanceResult.proposedTrades} />
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
