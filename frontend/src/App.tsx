import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fetchStatus } from './api/status'
import Navbar from './components/layout/Navbar'
import ProtectedRoute from './components/layout/ProtectedRoute'
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import PatientDashboard from './pages/dashboard/PatientDashboard'
import { useAuthStore } from './store/authStore'

function LandingHome() {
  const { isAuthenticated } = useAuthStore()
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['status'],
    queryFn: fetchStatus,
  })

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-gradient-to-br from-primary-950 via-primary-900 to-primary-800 flex items-center justify-center p-4">
      <div className="w-full max-w-lg">
        {/* ── Logo & Title ── */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-20 h-20 rounded-2xl bg-white/10 backdrop-blur-sm mb-4">
            <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h1 className="text-4xl font-bold text-white tracking-tight">CURA</h1>
          <p className="text-primary-300 mt-2 text-sm font-medium uppercase tracking-widest">
            Telehealth · EMR · Emergency
          </p>
        </div>

        {/* ── Auth CTA Banner ── */}
        <div className="mb-6 bg-white/10 backdrop-blur-sm rounded-2xl border border-white/20 p-5 text-center shadow-lg">
          {isAuthenticated ? (
            <div>
              <p className="text-white text-sm font-medium mb-3">You are currently signed in.</p>
              <Link
                to="/dashboard"
                className="inline-flex items-center gap-2 px-5 py-2.5 bg-primary-500 hover:bg-primary-600 text-white font-semibold text-xs rounded-xl shadow-md transition-colors"
              >
                Go to Portal Dashboard →
              </Link>
            </div>
          ) : (
            <div>
              <p className="text-white text-sm font-medium mb-3">Sign in or register to access clinical modules.</p>
              <div className="flex justify-center gap-3">
                <Link
                  to="/login"
                  className="px-5 py-2 bg-white text-primary-900 hover:bg-primary-50 font-bold text-xs rounded-xl shadow-md transition-colors"
                >
                  Sign in
                </Link>
                <Link
                  to="/register"
                  className="px-5 py-2 bg-primary-600 hover:bg-primary-700 text-white font-bold text-xs rounded-xl shadow-md transition-colors border border-primary-400"
                >
                  Register
                </Link>
              </div>
            </div>
          )}
        </div>

        {/* ── Status Card ── */}
        <div className="bg-white/10 backdrop-blur-sm rounded-2xl border border-white/20 p-6 shadow-2xl">
          <h2 className="text-white font-semibold text-base mb-4 flex items-center gap-2">
            <span className="inline-block w-2 h-2 rounded-full bg-primary-300"></span>
            Backend Connectivity
          </h2>

          {isLoading && (
            <div className="flex items-center gap-3 text-primary-200 text-sm">
              <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"/>
              </svg>
              Connecting to CURA backend…
            </div>
          )}

          {isError && (
            <div className="rounded-xl bg-danger-500/20 border border-danger-500/30 p-4">
              <p className="text-danger-200 font-semibold text-sm mb-1">⚠ Backend unreachable</p>
              <p className="text-danger-300 text-xs">
                {error instanceof Error ? error.message : 'Connection failed'}
              </p>
            </div>
          )}

          {data && (
            <div className="space-y-3">
              <div className="flex items-center gap-2">
                <span className="inline-block w-2.5 h-2.5 rounded-full bg-success-500 animate-pulse"></span>
                <span className="text-success-400 font-semibold text-sm">Connected</span>
              </div>
              <div className="grid grid-cols-2 gap-3 mt-3">
                {[
                  { label: 'Service', value: data.service },
                  { label: 'Status',  value: data.status },
                  { label: 'Version', value: data.version },
                  { label: 'Server Time', value: new Date(data.timestamp).toLocaleTimeString() },
                ].map(({ label, value }) => (
                  <div key={label} className="bg-white/5 rounded-xl p-3">
                    <p className="text-primary-400 text-[10px] uppercase tracking-wide mb-1">{label}</p>
                    <p className="text-white font-medium text-xs">{value}</p>
                  </div>
                ))}
              </div>
              <p className="text-primary-400 text-xs mt-3 border-t border-white/10 pt-3">
                ⚠ {data.disclaimer}
              </p>
            </div>
          )}
        </div>

        {/* ── Milestone marker ── */}
        <p className="text-center text-primary-400 text-xs mt-6">
          Milestone 2 — Authentication & JWT Active
        </p>
      </div>
    </div>
  )
}

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar />
        <main className="flex-1">
          <Routes>
            <Route path="/" element={<LandingHome />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<PatientDashboard />} />
            </Route>
            <Route path="*" element={<LandingHome />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}

export default App
