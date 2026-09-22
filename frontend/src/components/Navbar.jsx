import React, { useState, useEffect } from 'react';
import { NavLink, Link } from 'react-router-dom';
import { 
  ShieldCheck, 
  LayoutDashboard, 
  CreditCard, 
  History, 
  Server,
  Activity
} from 'lucide-react';
import { getHealth } from '../services/api';

export default function Navbar() {
  const [backendHealthy, setBackendHealthy] = useState(null);

  useEffect(() => {
    const checkHealth = async () => {
      try {
        await getHealth();
        setBackendHealthy(true);
      } catch {
        setBackendHealthy(false);
      }
    };
    checkHealth();
    const interval = setInterval(checkHealth, 30000);
    return () => clearInterval(interval);
  }, []);

  const navItemClass = ({ isActive }) =>
    `inline-flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold transition-all ${
      isActive
        ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/30 shadow-sm'
        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
    }`;

  return (
    <header className="sticky top-0 z-40 w-full backdrop-blur-md bg-slate-950/80 border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <Link to="/" className="flex items-center gap-3 group">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 flex items-center justify-center shadow-lg shadow-indigo-500/25 group-hover:scale-105 transition-transform">
            <ShieldCheck className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="text-sm font-black tracking-tight text-white flex items-center gap-1.5 font-mono">
              POROUTER <span className="text-[10px] font-sans font-semibold px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">MOCK</span>
            </div>
            <div className="text-[10px] text-slate-400 font-medium">Payment Orchestration Router</div>
          </div>
        </Link>

        {/* Navigation Links */}
        <nav className="hidden md:flex items-center gap-1">
          <NavLink to="/" className={navItemClass}>
            <LayoutDashboard className="w-4 h-4" />
            Dashboard
          </NavLink>
          <NavLink to="/process" className={navItemClass}>
            <CreditCard className="w-4 h-4" />
            Process Payment
          </NavLink>
          <NavLink to="/history" className={navItemClass}>
            <History className="w-4 h-4" />
            Audit History
          </NavLink>
          <NavLink to="/gateways" className={navItemClass}>
            <Server className="w-4 h-4" />
            Gateways
          </NavLink>
        </nav>

        {/* Right side: Backend Health Pill */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-900 border border-slate-800 text-xs font-mono">
            <Activity className="w-3.5 h-3.5 text-slate-400" />
            <span className="text-slate-400 hidden sm:inline">Backend:</span>
            {backendHealthy === null ? (
              <span className="text-slate-500">Checking...</span>
            ) : backendHealthy ? (
              <span className="flex items-center gap-1.5 text-emerald-400 font-semibold">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                UP
              </span>
            ) : (
              <span className="flex items-center gap-1.5 text-rose-400 font-semibold">
                <span className="w-2 h-2 rounded-full bg-rose-400" />
                DOWN
              </span>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
