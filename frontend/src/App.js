import React, { useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import axios from 'axios';
import './App.css';

const API_BASE = 'http://localhost:8080/stock';

const INTERVALS = [
  { key: 'daily',    label: 'Diario' },
  { key: 'weekly',   label: 'Semanal' },
  { key: 'monthly',  label: 'Mensual' },
  { key: 'intraday', label: 'Intradiario' },
];

function App() {
  const [symbol, setSymbol]     = useState('');
  const [interval, setInterval] = useState('daily');
  const [data, setData]         = useState(null);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');

  const fetchData = async () => {
    if (!symbol.trim()) return;
    setLoading(true);
    setError('');
    setData(null);
    try {
      const res = await axios.get(`${API_BASE}/${interval}?symbol=${symbol.toUpperCase()}`);
      const prices = res.data.prices;
      // Convertir el mapa fecha→precio a array para recharts, ordenado por fecha
      const chartData = Object.entries(prices)
        .sort(([a], [b]) => a.localeCompare(b))
        .slice(-50) // últimas 50 entradas para no saturar el gráfico
        .map(([date, price]) => ({
          date: date.substring(0, 10), // recortar a yyyy-MM-dd
          precio: parseFloat(price.toFixed(2)),
        }));
      setData({ symbol: res.data.symbol, interval: res.data.interval, chartData });
    } catch (e) {
      setError('Error al obtener datos. Verifica el símbolo o intenta con IBM.');
    } finally {
      setLoading(false);
    }
  };

  const handleKey = (e) => {
    if (e.key === 'Enter') fetchData();
  };

  return (
    <div className="app">
      <header className="header">
        <h1 className="title">Stock <span className="accent">Viewer</span></h1>
        <p className="subtitle">Consulta histórico de acciones en bolsa</p>
      </header>

      <main className="main">
        {/* Controles */}
        <div className="controls">
          <input
            className="input"
            type="text"
            placeholder="Símbolo (ej: IBM)"
            value={symbol}
            onChange={e => setSymbol(e.target.value.toUpperCase())}
            onKeyDown={handleKey}
            maxLength={10}
          />
          <div className="interval-group">
            {INTERVALS.map(iv => (
              <button
                key={iv.key}
                className={`interval-btn ${interval === iv.key ? 'active' : ''}`}
                onClick={() => setInterval(iv.key)}
              >
                {iv.label}
              </button>
            ))}
          </div>
          <button className="search-btn" onClick={fetchData} disabled={loading}>
            {loading ? 'Cargando...' : 'Buscar'}
          </button>
        </div>

        {/* Error */}
        {error && <div className="error">{error}</div>}

        {/* Gráfico */}
        {data && (
          <div className="chart-wrapper">
            <div className="chart-header">
              <span className="chart-symbol">{data.symbol}</span>
              <span className="chart-interval">{data.interval}</span>
              <span className="chart-count">{data.chartData.length} registros</span>
            </div>

            {data.chartData.length === 0 ? (
              <div className="empty">Sin datos disponibles para este símbolo.</div>
            ) : (
              <ResponsiveContainer width="100%" height={380}>
                <BarChart data={data.chartData} margin={{ top: 10, right: 20, left: 10, bottom: 60 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
                  <XAxis
                    dataKey="date"
                    tick={{ fontSize: 11, fill: '#666' }}
                    angle={-45}
                    textAnchor="end"
                    interval="preserveStartEnd"
                  />
                  <YAxis
                    tick={{ fontSize: 11, fill: '#666' }}
                    tickFormatter={v => `$${v}`}
                    domain={['auto', 'auto']}
                  />
                  <Tooltip
                    formatter={(value) => [`$${value}`, 'Precio cierre']}
                    labelFormatter={(label) => `Fecha: ${label}`}
                    contentStyle={{ borderRadius: '6px', fontSize: '13px' }}
                  />
                  <Bar dataKey="precio" fill="#2563eb" radius={[3, 3, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        )}

        {/* Estado inicial */}
        {!data && !loading && !error && (
          <div className="placeholder">
            Ingresa un símbolo y selecciona un intervalo para ver el histórico
          </div>
        )}
      </main>
    </div>
  );
}

export default App;