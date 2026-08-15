import React from 'react';

const COLORS = {
  EQUITY: '#1d4ed8',
  FIXED_INCOME: '#059669',
  CASH: '#9ca3af',
};

function AllocationBar({ label, weights }) {
  return (
    <div style={{ marginBottom: 12 }}>
      <div style={{ fontSize: 13, color: '#6b7280', marginBottom: 4 }}>{label}</div>
      <div style={{ display: 'flex', height: 24, borderRadius: 4, overflow: 'hidden' }}>
        {Object.entries(weights).map(([assetClass, weight]) => (
          <div
            key={assetClass}
            title={`${assetClass}: ${(weight * 100).toFixed(1)}%`}
            style={{
              width: `${weight * 100}%`,
              backgroundColor: COLORS[assetClass] || '#ccc',
            }}
          />
        ))}
      </div>
      <div style={{ display: 'flex', gap: 16, marginTop: 6, fontSize: 12 }}>
        {Object.entries(weights).map(([assetClass, weight]) => (
          <span key={assetClass}>
            <span style={{ color: COLORS[assetClass] }}>●</span> {assetClass} {(weight * 100).toFixed(1)}%
          </span>
        ))}
      </div>
    </div>
  );
}

export default AllocationBar;
