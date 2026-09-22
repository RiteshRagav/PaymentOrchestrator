import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  CreditCard, 
  CheckCircle2, 
  XCircle, 
  TrendingUp, 
  Server, 
  ArrowRight, 
  RefreshCw,
  Clock,
  ShieldCheck,
  AlertTriangle
} from 'lucide-react';
import { getPaymentStats, getAllPayments, getGatewayStatus } from '../services/api';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [recentPayments, setRecentPayments] = useState([]);
  const [gateways, setGateways] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [refreshing, setRefreshing] = useState(false);

  const fetchData = async (isManualRefresh = false) => {
    if (isManualRefresh) setRefreshing(true);
    try {
      setError(null);
      const [statsRes, paymentsRes, gatewaysRes] = await Promise.all([
        getPaymentStats().catch(() => ({ data: { totalPayments: 0, successfulPayments: 0, failedPayments: 0, successRate: 0 } })),
        getAllPayments(0, 5).catch(() => ({ data: { content: [] } })),
        getGatewayStatus().catch(() => ({ data: [] }))
      ]);

      setStats(statsRes.data);
      setRecentPayments(paymentsRes.data?.content || []);
      setGateways(gatewaysRes.data || []);
    } catch (err) {
      setError('Failed to load dashboard metrics. Ensure the backend is running.');
    } finally {
      setLoading(false);
      if (isManualRefresh) setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(() => fetchData(), 15000);
    return () => clearInterval(interval);
  }, []);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SUCCESS':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/15 text-emerald-400 border border-emerald-500/20"><CheckCircle2 className="w-3 h-3" /> Success</span>;
      case 'FAILED':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-500/15 text-rose-400 border border-rose-500/20"><XCircle className="w-3 h-3" /> Failed</span>;
      case 'TIMEOUT':
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/15 text-amber-400 border border-amber-500/20"><AlertTriangle className="w-3 h-3" /> Timeout</span>;
      default:
        return <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-blue-500/15 text-blue-400 border border-blue-500/20"><Clock className="w-3 h-3" /> {status}</span>;
    }
  };

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 pb-2 border-b border-slate-800">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold tracking-tight text-white flex items-center gap-3">
            <span className="p-2 rounded-lg bg-indigo-600/20 text-indigo-400 border border-indigo-500/30">
              <ShieldCheck className="w-6 h-6" />
            </span>
            Payment Orchestration Router
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Intelligent routing, multi-gateway failover, and real-time transaction telemetry.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => fetchData(true)}
            disabled={refreshing}
            className="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors border border-slate-700 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin text-indigo-400' : ''}`} />
            {refreshing ? 'Refreshing...' : 'Refresh'}
          </button>
          <Link
            to="/process"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/25 transition-all"
          >
            <CreditCard className="w-4 h-4" />
            Simulate Payment
          </Link>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-300 text-sm flex items-center gap-3">
          <AlertTriangle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Payments */}
        <div className="p-5 rounded-xl bg-slate-900/70 border border-slate-800 backdrop-blur-sm relative overflow-hidden group hover:border-slate-700 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Total Volume</span>
            <div className="p-2 rounded-lg bg-blue-500/10 text-blue-400">
              <CreditCard className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-white font-mono">
              {loading ? '...' : (stats?.totalPayments ?? 0).toLocaleString()}
            </div>
            <p className="text-xs text-slate-500 mt-1">Processed transactions</p>
          </div>
          <div className="absolute inset-x-0 bottom-0 h-1 bg-gradient-to-r from-blue-500 to-indigo-500 opacity-50" />
        </div>

        {/* Success Rate */}
        <div className="p-5 rounded-xl bg-slate-900/70 border border-slate-800 backdrop-blur-sm relative overflow-hidden group hover:border-slate-700 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Success Rate</span>
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-emerald-400 font-mono">
              {loading ? '...' : `${Number(stats?.successRate || 0).toFixed(1)}%`}
            </div>
            <p className="text-xs text-slate-500 mt-1">Overall delivery fidelity</p>
          </div>
          <div className="absolute inset-x-0 bottom-0 h-1 bg-gradient-to-r from-emerald-500 to-teal-500 opacity-50" />
        </div>

        {/* Successful Transactions */}
        <div className="p-5 rounded-xl bg-slate-900/70 border border-slate-800 backdrop-blur-sm relative overflow-hidden group hover:border-slate-700 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Successful</span>
            <div className="p-2 rounded-lg bg-teal-500/10 text-teal-400">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-white font-mono">
              {loading ? '...' : (stats?.successfulPayments ?? 0).toLocaleString()}
            </div>
            <p className="text-xs text-slate-500 mt-1">Confirmed approvals</p>
          </div>
          <div className="absolute inset-x-0 bottom-0 h-1 bg-gradient-to-r from-teal-500 to-emerald-500 opacity-50" />
        </div>

        {/* Failed / Fallbacks */}
        <div className="p-5 rounded-xl bg-slate-900/70 border border-slate-800 backdrop-blur-sm relative overflow-hidden group hover:border-slate-700 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">Terminal Failures</span>
            <div className="p-2 rounded-lg bg-rose-500/10 text-rose-400">
              <XCircle className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-rose-400 font-mono">
              {loading ? '...' : (stats?.failedPayments ?? 0).toLocaleString()}
            </div>
            <p className="text-xs text-slate-500 mt-1">Exhausted all fallbacks</p>
          </div>
          <div className="absolute inset-x-0 bottom-0 h-1 bg-gradient-to-r from-rose-500 to-amber-500 opacity-50" />
        </div>
      </div>

      {/* Main Grid: Gateways + Recent Payments */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Gateway Pipeline Status */}
        <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Server className="w-4 h-4 text-indigo-400" />
                <h2 className="text-base font-semibold text-white">Gateway Routing Chain</h2>
              </div>
              <Link to="/gateways" className="text-xs text-indigo-400 hover:text-indigo-300 flex items-center gap-1 font-medium">
                View All <ArrowRight className="w-3 h-3" />
              </Link>
            </div>

            <p className="text-xs text-slate-400 mt-3 mb-5">
              The router cascades through active gateways ordered by priority upon timeout or failure.
            </p>

            <div className="space-y-3">
              {gateways.length === 0 ? (
                <div className="text-center py-6 text-xs text-slate-500">
                  No gateway telemetry found. Check backend connection.
                </div>
              ) : (
                gateways.map((gw) => (
                  <div 
                    key={gw.name}
                    className="p-3.5 rounded-xl bg-slate-800/50 border border-slate-700/60 flex items-center justify-between hover:bg-slate-800/80 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <span className="w-6 h-6 rounded-full bg-slate-700 text-slate-300 text-xs font-mono font-bold flex items-center justify-center">
                        {gw.priority}
                      </span>
                      <div>
                        <div className="text-sm font-semibold text-white">{gw.name}</div>
                        <div className="text-xs text-slate-400">
                          {gw.latencyMs}ms avg latency • {Math.round((gw.failureRate || 0) * 100)}% simulated failure
                        </div>
                      </div>
                    </div>
                    <div>
                      {gw.enabled ? (
                        <span className="px-2 py-0.5 rounded-full text-[11px] font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                          Active
                        </span>
                      ) : (
                        <span className="px-2 py-0.5 rounded-full text-[11px] font-medium bg-slate-700 text-slate-400">
                          Disabled
                        </span>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-slate-800/80">
            <Link
              to="/process"
              className="w-full inline-flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium border border-slate-700 transition-all"
            >
              Test Fallback Workflow <ArrowRight className="w-3 h-3" />
            </Link>
          </div>
        </div>

        {/* Recent Transactions Table */}
        <div className="lg:col-span-2 p-6 rounded-2xl bg-slate-900/80 border border-slate-800">
          <div className="flex items-center justify-between pb-4 border-b border-slate-800">
            <div className="flex items-center gap-2">
              <CreditCard className="w-4 h-4 text-indigo-400" />
              <h2 className="text-base font-semibold text-white">Recent Transactions</h2>
            </div>
            <Link to="/history" className="text-xs text-indigo-400 hover:text-indigo-300 flex items-center gap-1 font-medium">
              Full Audit Trail <ArrowRight className="w-3 h-3" />
            </Link>
          </div>

          <div className="overflow-x-auto mt-4">
            <table className="w-full text-left">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 text-xs uppercase tracking-wider">
                  <th className="py-2.5 px-3">Payment ID</th>
                  <th className="py-2.5 px-3">Customer</th>
                  <th className="py-2.5 px-3">Amount</th>
                  <th className="py-2.5 px-3">Status</th>
                  <th className="py-2.5 px-3">Gateway</th>
                  <th className="py-2.5 px-3">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-xs">
                {recentPayments.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-slate-500">
                      No payments recorded yet. Simulate your first payment to observe the router!
                    </td>
                  </tr>
                ) : (
                  recentPayments.map((p) => (
                    <tr key={p.paymentId} className="hover:bg-slate-800/40 transition-colors">
                      <td className="py-3 px-3 font-mono font-medium text-indigo-300">
                        {p.paymentId}
                      </td>
                      <td className="py-3 px-3 text-slate-300 font-mono">
                        {p.customerId}
                      </td>
                      <td className="py-3 px-3 font-mono font-semibold text-white">
                        {p.currency} {Number(p.amount).toFixed(2)}
                      </td>
                      <td className="py-3 px-3">
                        {getStatusBadge(p.status)}
                      </td>
                      <td className="py-3 px-3 text-slate-300">
                        {p.gateway ? (
                          <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-200 border border-slate-700">
                            {p.gateway}
                          </span>
                        ) : (
                          <span className="text-slate-500 italic">None</span>
                        )}
                      </td>
                      <td className="py-3 px-3 text-slate-400 font-mono text-[11px]">
                        {new Date(p.createdAt).toLocaleTimeString()}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
