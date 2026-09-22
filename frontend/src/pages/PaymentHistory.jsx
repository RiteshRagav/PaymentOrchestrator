import React, { useState, useEffect } from 'react';
import { 
  CreditCard, 
  Search, 
  Filter, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  AlertTriangle,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  Eye,
  X
} from 'lucide-react';
import { getAllPayments, getPaymentsByStatus, getPayment, parseGateways } from '../services/api';

export default function PaymentHistory() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [modalLoading, setModalLoading] = useState(false);

  const fetchPayments = async () => {
    setLoading(true);
    try {
      let res;
      if (statusFilter === 'ALL') {
        res = await getAllPayments(page, 10);
      } else {
        res = await getPaymentsByStatus(statusFilter, page, 10);
      }
      setPayments(res.data.content || []);
      setTotalPages(res.data.totalPages || 1);
      setTotalElements(res.data.totalElements || 0);
    } catch (err) {
      console.error('Failed to load payments', err);
      setPayments([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayments();
  }, [page, statusFilter]);

  const handleStatusChange = (newStatus) => {
    setStatusFilter(newStatus);
    setPage(0);
  };

  const viewPaymentDetails = async (paymentId) => {
    setModalLoading(true);
    try {
      const res = await getPayment(paymentId);
      setSelectedPayment(res.data);
    } catch (err) {
      console.error('Error fetching payment details', err);
    } finally {
      setModalLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SUCCESS':
        return <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/15 text-emerald-400 border border-emerald-500/20"><CheckCircle2 className="w-3 h-3" /> Success</span>;
      case 'FAILED':
        return <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/15 text-rose-400 border border-rose-500/20"><XCircle className="w-3 h-3" /> Failed</span>;
      case 'TIMEOUT':
        return <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/15 text-amber-400 border border-amber-500/20"><AlertTriangle className="w-3 h-3" /> Timeout</span>;
      default:
        return <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-500/15 text-blue-400 border border-blue-500/20"><Clock className="w-3 h-3" /> {status}</span>;
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4 border-b border-slate-800">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-3">
            <span className="p-2 rounded-lg bg-indigo-600/20 text-indigo-400 border border-indigo-500/30">
              <CreditCard className="w-6 h-6" />
            </span>
            Transaction Audit Trail
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Historical transaction records, settled gateways, and routing logs.
          </p>
        </div>

        <button
          onClick={fetchPayments}
          disabled={loading}
          className="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors border border-slate-700"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-indigo-400' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="flex flex-wrap items-center gap-2">
        {['ALL', 'SUCCESS', 'FAILED', 'TIMEOUT', 'PENDING'].map((status) => (
          <button
            key={status}
            onClick={() => handleStatusChange(status)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              statusFilter === status
                ? 'bg-indigo-600 text-white font-semibold shadow-sm'
                : 'bg-slate-900 text-slate-400 hover:bg-slate-800 hover:text-slate-200 border border-slate-800'
            }`}
          >
            {status}
          </button>
        ))}
        <span className="ml-auto text-xs text-slate-500">
          Showing {payments.length} of {totalElements} total
        </span>
      </div>

      {/* Table */}
      <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 text-xs uppercase tracking-wider">
                <th className="py-3 px-4">Payment ID</th>
                <th className="py-3 px-4">Customer</th>
                <th className="py-3 px-4">Amount</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Gateway</th>
                <th className="py-3 px-4">Attempts</th>
                <th className="py-3 px-4">Timestamp</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-xs">
              {loading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto text-indigo-400 mb-2" />
                    Loading payment records...
                  </td>
                </tr>
              ) : payments.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    No transactions matching filter "{statusFilter}".
                  </td>
                </tr>
              ) : (
                payments.map((p) => (
                  <tr key={p.paymentId} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3 px-4 font-mono font-medium text-indigo-300">
                      {p.paymentId}
                    </td>
                    <td className="py-3 px-4 text-slate-300 font-mono">
                      {p.customerId}
                    </td>
                    <td className="py-3 px-4 font-mono font-semibold text-white">
                      {p.currency} {Number(p.amount).toFixed(2)}
                    </td>
                    <td className="py-3 px-4">
                      {getStatusBadge(p.status)}
                    </td>
                    <td className="py-3 px-4 text-slate-300">
                      {p.gateway ? (
                        <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-200 border border-slate-700">
                          {p.gateway}
                        </span>
                      ) : (
                        <span className="text-slate-500 italic">None</span>
                      )}
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-400">
                      {parseGateways(p.attemptedGateways).length || 1}
                    </td>
                    <td className="py-3 px-4 text-slate-400 font-mono text-[11px]">
                      {new Date(p.createdAt).toLocaleString()}
                    </td>
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => viewPaymentDetails(p.paymentId)}
                        className="p-1.5 rounded-lg text-slate-400 hover:text-indigo-300 hover:bg-indigo-600/20 transition-all"
                        title="View Details"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="flex items-center justify-between pt-4 mt-2 border-t border-slate-800 text-xs text-slate-400">
          <div>
            Page <span className="font-semibold text-white">{page + 1}</span> of{' '}
            <span className="font-semibold text-white">{totalPages || 1}</span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-1 transition-colors"
            >
              <ChevronLeft className="w-3.5 h-3.5" /> Prev
            </button>
            <button
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-1 transition-colors"
            >
              Next <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>

      {/* Details Modal */}
      {selectedPayment && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-lg rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl p-6 relative space-y-4">
            <button
              onClick={() => setSelectedPayment(null)}
              className="absolute top-4 right-4 p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3">
              <span className="p-2 rounded-lg bg-indigo-600/20 text-indigo-400">
                <CreditCard className="w-5 h-5" />
              </span>
              <div>
                <h3 className="text-base font-bold text-white">Payment Audit Record</h3>
                <p className="text-xs font-mono text-slate-400">{selectedPayment.paymentId}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs pt-2">
              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500">Status</span>
                <div className="mt-1">{getStatusBadge(selectedPayment.status)}</div>
              </div>

              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500">Amount</span>
                <div className="mt-1 font-mono font-bold text-white text-sm">
                  {selectedPayment.currency} {Number(selectedPayment.amount).toFixed(2)}
                </div>
              </div>

              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500">Settled Gateway</span>
                <div className="mt-1 font-semibold text-slate-200">
                  {selectedPayment.gateway || 'None'}
                </div>
              </div>

              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500">Customer ID</span>
                <div className="mt-1 font-mono text-slate-300">
                  {selectedPayment.customerId}
                </div>
              </div>
            </div>

            {/* Attempted Gateways Cascade */}
            {parseGateways(selectedPayment.attemptedGateways).length > 0 && (
              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800 text-xs">
                <span className="text-slate-500 block mb-2">Evaluated Gateway Sequence</span>
                <div className="flex flex-wrap gap-2">
                  {parseGateways(selectedPayment.attemptedGateways).map((gw) => (
                    <span 
                      key={gw}
                      className={`px-2 py-1 rounded font-mono text-xs border ${
                        gw === selectedPayment.gateway
                          ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30'
                          : 'bg-slate-800 text-slate-300 border-slate-700'
                      }`}
                    >
                      {gw} {gw === selectedPayment.gateway ? '(Success)' : '(Failed/Skipped)'}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {selectedPayment.message && (
              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800 text-xs">
                <span className="text-slate-500 block">Router Message</span>
                <p className="text-slate-300 mt-1 font-mono text-[11px]">{selectedPayment.message}</p>
              </div>
            )}

            <div className="pt-2 text-right">
              <button
                onClick={() => setSelectedPayment(null)}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
