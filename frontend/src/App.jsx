import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import ProcessPayment from './pages/ProcessPayment';
import PaymentHistory from './pages/PaymentHistory';
import GatewayStatus from './pages/GatewayStatus';

import ErrorBoundary from './components/ErrorBoundary';

export default function App() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col selection:bg-indigo-500 selection:text-white">
      <Navbar />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <ErrorBoundary>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/process" element={<ProcessPayment />} />
            <Route path="/history" element={<PaymentHistory />} />
            <Route path="/gateways" element={<GatewayStatus />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </ErrorBoundary>
      </main>

      <footer className="border-t border-slate-900 bg-slate-950/50 py-6 text-center text-xs text-slate-600">
        <div className="max-w-7xl mx-auto px-4">
          <p>Educational Portfolio Project — Payment Orchestration Router Layer with Dynamic Fallback.</p>
          <p className="mt-1 text-slate-700">Strictly for demonstration. Does not process real financial assets or connect to production networks.</p>
        </div>
      </footer>
    </div>
  );
}
