import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Response interceptor for global error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || error.message || 'An error occurred';
    return Promise.reject({ ...error, displayMessage: message });
  }
);

// ---- Payment APIs ----

export const processPayment = (paymentRequest, idempotencyKey = null) => {
  const headers = {};
  if (idempotencyKey) headers['Idempotency-Key'] = idempotencyKey;
  return apiClient.post('/payments', paymentRequest, { headers });
};

export const getPayment = (paymentId) =>
  apiClient.get(`/payments/${paymentId}`);

export const getAllPayments = (page = 0, size = 20) =>
  apiClient.get('/payments', { params: { page, size } });

export const getPaymentsByStatus = (status, page = 0, size = 20) =>
  apiClient.get(`/payments/status/${status}`, { params: { page, size } });

export const getPaymentStats = () =>
  apiClient.get('/payments/stats');

// ---- Gateway APIs ----

export const getGatewayStatus = () =>
  apiClient.get('/gateways');

// ---- Health API ----

export const getHealth = () =>
  apiClient.get('/health');

// ---- Helper Utilities ----

export const parseGateways = (val) => {
  if (!val) return [];
  if (Array.isArray(val)) return val.filter(Boolean);
  if (typeof val === 'string') {
    return val.split(',').map(s => s.trim()).filter(Boolean);
  }
  return [];
};

export default apiClient;
