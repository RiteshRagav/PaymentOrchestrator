import React, { useState, useEffect } from 'react';
import { 
  Server, 
  Activity, 
  Clock, 
  Percent, 
  ShieldCheck, 
  RefreshCw,
  Zap,
  ArrowDown
} from 'lucide-react';
import { getGatewayStatus } from '../services/api';

export default function GatewayStatus() {
  const [gateways, setGateways] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchGateways = async () => {
    setLoading(true);
    try {
      const res = await getGatewayStatus();
      // Sort gateways by priority ascending
      const sorted = (res.data || []).sort((a, b) => a.priority - b.priority);
      setGateways(sorted);
    } catch (err) {
      setError('Unable to fetch live gateway telemetry. Ensure backend is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGateways();
  }, []);

  return (
    <div className="space-y-8 animate-fade-in max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-3">
            <span className="p-2 rounded-lg bg-indigo-600/20 text-indigo-400 border border-indigo-500/30">
              <Server className="w-6 h-6" />
            </span>
            Mock Payment Gateways & Routing Cascade
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Real-time status and operational parameters for all registered payment processors.
          </p>
        </div>

        <button
          onClick={fetchGateways}
          disabled={loading}
          className="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors border border-slate-700"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-indigo-400' : ''}`} />
          Refresh Status
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-300 text-sm">
          {error}
        </div>
      )}

      {/* Gateway Cascade Cards */}
      <div className="space-y-4">
        {loading ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto text-indigo-400 mb-2" />
            Loading registered gateways...
          </div>
        ) : gateways.length === 0 ? (
          <div className="p-8 text-center text-slate-500 bg-slate-900/50 rounded-2xl border border-slate-800">
            No gateways currently discovered in the application context.
          </div>
        ) : (
          gateways.map((gw, idx) => (
            <React.Fragment key={gw.name}>
              <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 hover:border-slate-700 transition-all relative overflow-hidden">
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                  {/* Left info */}
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-xl bg-indigo-600/10 border border-indigo-500/20 flex flex-col items-center justify-center">
                      <span className="text-[10px] uppercase font-bold text-slate-400">Prio</span>
                      <span className="text-lg font-black text-indigo-400 font-mono leading-none">
                        {gw.priority}
                      </span>
                    </div>

                    <div>
                      <div className="flex items-center gap-3">
                        <h2 className="text-lg font-bold text-white tracking-wide">{gw.name}</h2>
                        {gw.enabled ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/15 text-emerald-400 border border-emerald-500/20">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                            Operational
                          </span>
                        ) : (
                          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-700 text-slate-400">
                            Disabled
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-slate-400 mt-1">
                        {gw.priority === 1 && 'Primary gateway. Router attempts this provider first for every new payment.'}
                        {gw.priority === 2 && 'Secondary fallback provider. Activated when primary gateway fails or times out.'}
                        {gw.priority === 3 && 'Tertiary fallback provider. High reliability target to rescue failing flows.'}
                      </p>
                    </div>
                  </div>

                  {/* Right metrics */}
                  <div className="grid grid-cols-2 gap-3 sm:flex sm:items-center sm:gap-6 text-xs font-mono">
                    <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800/80">
                      <span className="text-slate-500 text-[10px] block uppercase font-sans">Simulated Latency</span>
                      <span className="font-bold text-slate-200 flex items-center gap-1 mt-0.5">
                        <Clock className="w-3.5 h-3.5 text-indigo-400" />
                        {gw.latencyMs} ms
                      </span>
                    </div>

                    <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800/80">
                      <span className="text-slate-500 text-[10px] block uppercase font-sans">Failure Rate</span>
                      <span className="font-bold text-amber-300 flex items-center gap-1 mt-0.5">
                        <Percent className="w-3.5 h-3.5 text-amber-400" />
                        {Math.round((gw.failureRate || 0) * 100)}%
                      </span>
                    </div>
                  </div>
                </div>

                <div className={`absolute top-0 left-0 w-1.5 h-full ${
                  gw.priority === 1 ? 'bg-indigo-500' : gw.priority === 2 ? 'bg-teal-500' : 'bg-amber-500'
                }`} />
              </div>

              {idx < gateways.length - 1 && (
                <div className="flex items-center justify-center py-1">
                  <div className="flex items-center gap-2 text-xs font-mono text-slate-500 bg-slate-900/60 px-3 py-1 rounded-full border border-slate-800">
                    <span>Failover Cascade</span>
                    <ArrowDown className="w-3.5 h-3.5 text-indigo-400 animate-bounce" />
                  </div>
                </div>
              )}
            </React.Fragment>
          ))
        )}
      </div>

      {/* Architecture overview card */}
      <div className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 space-y-3">
        <h3 className="text-sm font-semibold text-white flex items-center gap-2">
          <Zap className="w-4 h-4 text-amber-400" />
          How Smart Routing & Cascade Works
        </h3>
        <p className="text-xs text-slate-400 leading-relaxed">
          The Payment Orchestration Router collects all Spring beans implementing the <code className="text-indigo-300">PaymentGateway</code> interface. 
          When a request arrives, gateways are filtered by <code className="text-indigo-300">isEnabled()</code> and ordered by <code className="text-indigo-300">getPriority()</code> ascending. 
          The router sequentially attempts to execute each processor within a configurable timeout boundary. 
          If a gateway returns a decline or throws an exception, the audit log records the attempt and immediately cascades execution to the next fallback gateway.
        </p>
      </div>
    </div>
  );
}
