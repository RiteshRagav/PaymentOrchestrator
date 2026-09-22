import React, { useState } from 'react';
import { 
  CreditCard, 
  Send, 
  RefreshCw, 
  CheckCircle2, 
  XCircle, 
  AlertTriangle, 
  ArrowRight,
  Shield,
  Layers,
  Key,
  Clock
} from 'lucide-react';
import { processPayment, parseGateways } from '../services/api';

export default function ProcessPayment() {
  const [formData, setFormData] = useState({
    customerId: 'CUST-8042',
    amount: '1299.00',
    currency: 'INR',
    idempotencyKey: 'IDEMP-' + Math.random().toString(36).substring(2, 9).toUpperCase()
  });

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const generateIdempotencyKey = () => {
    setFormData(prev => ({
      ...prev,
      idempotencyKey: 'IDEMP-' + Math.random().toString(36).substring(2, 9).toUpperCase()
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);

    const payload = {
      customerId: formData.customerId,
      amount: parseFloat(formData.amount),
      currency: formData.currency
    };

    const startTime = performance.now();
    try {
      const response = await processPayment(payload, formData.idempotencyKey || null);
      const elapsedMs = Math.round(performance.now() - startTime);
      setResult({ ...response.data, roundtripMs: elapsedMs });
    } catch (err) {
      const elapsedMs = Math.round(performance.now() - startTime);
      setError({
        message: err.displayMessage || err.message || 'Payment processing failed',
        roundtripMs: elapsedMs,
        data: err.response?.data
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-fade-in">
      {/* Header */}
      <div className="pb-4 border-b border-slate-800">
        <h1 className="text-2xl font-bold text-white flex items-center gap-3">
          <span className="p-2 rounded-lg bg-indigo-600/20 text-indigo-400 border border-indigo-500/30">
            <CreditCard className="w-6 h-6" />
          </span>
          Process Payment & Routing Simulator
        </h1>
        <p className="text-sm text-slate-400 mt-1">
          Submit an orchestrated transaction to experience dynamic gateway fallback, error handling, and idempotency protection.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-8">
        {/* Left column: Form */}
        <div className="md:col-span-6 space-y-6">
          <form onSubmit={handleSubmit} className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-5">
            <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400 flex items-center gap-2">
              <Layers className="w-4 h-4 text-indigo-400" />
              Transaction Parameters
            </h2>

            {/* Customer ID */}
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">
                Customer Identifier
              </label>
              <input
                type="text"
                required
                value={formData.customerId}
                onChange={e => setFormData({ ...formData, customerId: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950/80 border border-slate-700 text-white text-sm focus:border-indigo-500 focus:outline-none font-mono"
                placeholder="e.g. CUST-1001"
              />
            </div>

            {/* Amount and Currency */}
            <div className="grid grid-cols-3 gap-3">
              <div className="col-span-2">
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Amount
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  value={formData.amount}
                  onChange={e => setFormData({ ...formData, amount: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950/80 border border-slate-700 text-white text-sm focus:border-indigo-500 focus:outline-none font-mono"
                  placeholder="0.00"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Currency
                </label>
                <select
                  value={formData.currency}
                  onChange={e => setFormData({ ...formData, currency: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950/80 border border-slate-700 text-white text-sm focus:border-indigo-500 focus:outline-none font-mono"
                >
                  <option value="INR">INR</option>
                  <option value="USD">USD</option>
                  <option value="EUR">EUR</option>
                  <option value="GBP">GBP</option>
                </select>
              </div>
            </div>

            {/* Idempotency Key */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-xs font-medium text-slate-300 flex items-center gap-1.5">
                  <Key className="w-3.5 h-3.5 text-amber-400" />
                  Idempotency Key (Optional)
                </label>
                <button
                  type="button"
                  onClick={generateIdempotencyKey}
                  className="text-[11px] text-indigo-400 hover:text-indigo-300 flex items-center gap-1"
                >
                  <RefreshCw className="w-3 h-3" /> New Key
                </button>
              </div>
              <input
                type="text"
                value={formData.idempotencyKey}
                onChange={e => setFormData({ ...formData, idempotencyKey: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950/80 border border-slate-700 text-white text-sm focus:border-indigo-500 focus:outline-none font-mono text-xs text-amber-300/90"
                placeholder="Unique client token"
              />
              <p className="text-[11px] text-slate-500 mt-1">
                Submitting twice with the same key returns the cached payment record without double charging.
              </p>
            </div>

            {/* Submit button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full mt-2 py-3 px-4 rounded-xl font-semibold text-sm text-white bg-indigo-600 hover:bg-indigo-500 shadow-lg shadow-indigo-600/30 flex items-center justify-center gap-2 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin text-white" />
                  Routing Through Gateways...
                </>
              ) : (
                <>
                  <Send className="w-4 h-4" />
                  Initiate Orchestrated Payment
                </>
              )}
            </button>
          </form>

          {/* Fallback Strategy Explanation */}
          <div className="p-4 rounded-xl bg-slate-900/40 border border-slate-800 text-xs text-slate-400 space-y-2">
            <div className="font-semibold text-slate-300 flex items-center gap-2">
              <Shield className="w-3.5 h-3.5 text-indigo-400" />
              Router Fallback Guarantee
            </div>
            <p>
              1. The router invokes <strong>MockGatewayA</strong> (Priority 1).<br/>
              2. If Gateway A encounters simulated latency timeout or failure, it cascades to <strong>MockGatewayB</strong> (Priority 2).<br/>
              3. If Gateway B also declines, it falls back to <strong>MockGatewayC</strong> (Priority 3, highest reliability).
            </p>
          </div>
        </div>

        {/* Right column: Execution Trace / Results */}
        <div className="md:col-span-6 space-y-6">
          <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 min-h-[420px] flex flex-col">
            <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400 pb-3 border-b border-slate-800 flex items-center justify-between">
              <span>Execution Telemetry</span>
              {(result || error) && (
                <span className="text-xs font-mono font-normal text-slate-400 flex items-center gap-1">
                  <Clock className="w-3.5 h-3.5" />
                  {(result?.roundtripMs || error?.roundtripMs)} ms
                </span>
              )}
            </h2>

            {loading ? (
              <div className="flex-1 flex flex-col items-center justify-center py-12 text-center space-y-4">
                <div className="relative">
                  <div className="w-16 h-16 rounded-full border-4 border-indigo-500/20 border-t-indigo-500 animate-spin" />
                  <CreditCard className="w-6 h-6 text-indigo-400 absolute inset-0 m-auto" />
                </div>
                <div>
                  <div className="text-sm font-medium text-white">Evaluating Gateway Waterfall</div>
                  <div className="text-xs text-slate-500 mt-1">Executing mock latency and health assertions...</div>
                </div>
              </div>
            ) : result ? (
              <div className="flex-1 flex flex-col justify-between pt-4 space-y-6">
                <div>
                  {/* Status Banner */}
                  <div className={`p-4 rounded-xl border flex items-center gap-3 ${
                    result.status === 'SUCCESS' 
                      ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300' 
                      : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
                  }`}>
                    {result.status === 'SUCCESS' ? (
                      <CheckCircle2 className="w-6 h-6 flex-shrink-0 text-emerald-400" />
                    ) : (
                      <XCircle className="w-6 h-6 flex-shrink-0 text-rose-400" />
                    )}
                    <div>
                      <div className="font-bold text-sm tracking-wide">
                        PAYMENT {result.status}
                      </div>
                      <div className="text-xs opacity-90 mt-0.5">
                        {result.message || 'Payment handled by orchestration router.'}
                      </div>
                    </div>
                  </div>

                  {/* Details grid */}
                  <div className="mt-4 grid grid-cols-2 gap-3 text-xs">
                    <div className="p-3 rounded-lg bg-slate-950/60 border border-slate-800">
                      <div className="text-slate-500">Payment Reference</div>
                      <div className="font-mono font-medium text-indigo-300 mt-0.5 truncate">
                        {result.paymentId}
                      </div>
                    </div>

                    <div className="p-3 rounded-lg bg-slate-950/60 border border-slate-800">
                      <div className="text-slate-500">Settled Gateway</div>
                      <div className="font-mono font-medium text-white mt-0.5">
                        {result.gateway || 'None (Failed)'}
                      </div>
                    </div>

                    <div className="p-3 rounded-lg bg-slate-950/60 border border-slate-800">
                      <div className="text-slate-500">Amount & Currency</div>
                      <div className="font-mono font-medium text-white mt-0.5">
                        {result.currency} {Number(result.amount).toFixed(2)}
                      </div>
                    </div>

                    <div className="p-3 rounded-lg bg-slate-950/60 border border-slate-800">
                      <div className="text-slate-500">Timestamp</div>
                      <div className="font-mono font-medium text-slate-300 mt-0.5 text-[11px]">
                        {new Date(result.createdAt).toLocaleTimeString()}
                      </div>
                    </div>
                  </div>

                  {/* Attempted Gateways Trail */}
                  {parseGateways(result.attemptedGateways).length > 0 && (
                    <div className="mt-4">
                      <div className="text-xs text-slate-400 font-medium mb-2">
                        Audit Trail (Evaluated Gateways):
                      </div>
                      <div className="flex flex-wrap items-center gap-2">
                        {parseGateways(result.attemptedGateways).map((gw, idx, arr) => {
                          const isFinal = gw === result.gateway;
                          return (
                            <React.Fragment key={gw}>
                              <span className={`px-2.5 py-1 rounded-md text-xs font-mono font-medium border ${
                                isFinal 
                                  ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40 font-bold' 
                                  : 'bg-rose-500/10 text-rose-300 border-rose-500/30'
                              }`}>
                                {gw} {isFinal ? '✓' : '✗'}
                              </span>
                              {idx < arr.length - 1 && (
                                <ArrowRight className="w-3 h-3 text-slate-600" />
                              )}
                            </React.Fragment>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </div>

                {/* Raw JSON */}
                <details className="mt-4 group">
                  <summary className="text-[11px] text-slate-500 hover:text-slate-400 cursor-pointer select-none">
                    View raw JSON response
                  </summary>
                  <pre className="mt-2 p-3 rounded-lg bg-slate-950 text-[11px] font-mono text-slate-300 overflow-x-auto border border-slate-800">
                    {JSON.stringify(result, null, 2)}
                  </pre>
                </details>
              </div>
            ) : error ? (
              <div className="flex-1 flex flex-col justify-center items-center py-8 text-center space-y-3">
                <AlertTriangle className="w-10 h-10 text-rose-500" />
                <div>
                  <div className="text-sm font-semibold text-rose-400">Request Error</div>
                  <div className="text-xs text-slate-400 mt-1 max-w-xs mx-auto">
                    {error.message}
                  </div>
                </div>
              </div>
            ) : (
              <div className="flex-1 flex flex-col items-center justify-center py-12 text-center text-slate-500 text-xs">
                <Send className="w-8 h-8 text-slate-700 mb-3" />
                Fill in parameters on the left and submit to view live routing results.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
