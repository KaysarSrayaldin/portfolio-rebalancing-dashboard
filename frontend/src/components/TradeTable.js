import React from 'react';

function TradeTable({ trades }) {
  if (!trades || trades.length === 0) {
    return <p>No trades required — portfolio is within target tolerance.</p>;
  }

  return (
    <table>
      <thead>
        <tr>
          <th>Action</th>
          <th>Ticker</th>
          <th>Quantity</th>
          <th>Est. Price</th>
          <th>Est. Value</th>
        </tr>
      </thead>
      <tbody>
        {trades.map((t, i) => (
          <tr key={i}>
            <td className={t.action === 'BUY' ? 'buy' : 'sell'}>{t.action}</td>
            <td>{t.ticker}</td>
            <td>{t.quantity}</td>
            <td>${Number(t.estimatedPrice).toFixed(2)}</td>
            <td>${Number(t.estimatedValue).toFixed(2)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default TradeTable;
