import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

export const getAccount = (id) => api.get(`/accounts/${id}`);
export const getHoldings = (id) => api.get(`/accounts/${id}/holdings`);
export const runRebalance = (id) => api.post(`/accounts/${id}/rebalance`);

export default api;
