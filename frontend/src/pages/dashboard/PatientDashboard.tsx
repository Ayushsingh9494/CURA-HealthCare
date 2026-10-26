import { useState } from 'react'
import { useAuthStore } from '../../store/authStore'
import { fetchCurrentUser } from '../../api/auth'

export default function PatientDashboard() {
  const { user, setUser } = useAuthStore()
  const [refreshing, setRefreshing] = useState(false)
  const [apiResponse, setApiResponse] = useState<string | null>(null)

  const handleTestProtectedApi = async () => {
    setRefreshing(true)
    setApiResponse(null)
    try {
      const profile = await fetchCurrentUser()
      setUser(profile)
      setApiResponse(JSON.stringify(profile, null, 2))
    } catch (err: any) {
      setApiResponse('Error: ' + (err.response?.data?.message || err.message))
    } finally {
      setRefreshing(false)
    }
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* ── Welcome Header ── */}
      <div className="bg-gradient-to-r from-primary-900 via-primary-800 to-primary-950 rounded-3xl p-6 sm:p-8 text-white shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 backdrop-blur-md text-xs font-semibold text-primary-200 mb-3 border border-white/10">
            <span className="w-2 h-2 rounded-full bg-success-500 animate-pulse"></span>
            Authenticated Session Active
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">
            Welcome, {user?.firstName ? `${user.firstName} ${user.lastName || ''}` : user?.email}
          </h1>
          <p className="text-primary-200 text-sm mt-1">
            CURA Capstone Prototype · Role: <span className="font-semibold text-white uppercase">{user?.role}</span>
          </p>
        </div>

        <div className="flex flex-wrap gap-3">
          <button
            onClick={handleTestProtectedApi}
            disabled={refreshing}
            className="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/20 border border-white/20 text-xs font-semibold backdrop-blur-sm transition-all"
          >
            {refreshing ? 'Verifying...' : 'Test GET /api/v1/auth/me (JWT)'}
          </button>
        </div>
      </div>

      {apiResponse && (
        <div className="bg-slate-900 text-slate-100 rounded-2xl p-4 font-mono text-xs overflow-x-auto border border-slate-800">
          <p className="text-primary-400 font-bold mb-2">// Server Response (Verified with JWT Bearer Token):</p>
          <pre>{apiResponse}</pre>
        </div>
      )}

      {/* ── Account Summary & Modules Overview ── */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Profile Card */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h2 className="text-base font-bold text-slate-900 mb-4 flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-primary-600"></span>
            Account Profile
          </h2>
          <div className="space-y-3 text-sm">
            <div>
              <p className="text-xs text-slate-400 font-medium">Email Address</p>
              <p className="text-slate-800 font-medium">{user?.email}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400 font-medium">Full Name</p>
              <p className="text-slate-800 font-medium">
                {user?.firstName ? `${user.firstName} ${user.lastName || ''}` : 'Not set'}
              </p>
            </div>
            <div>
              <p className="text-xs text-slate-400 font-medium">Phone</p>
              <p className="text-slate-800 font-medium">{user?.phone || 'Not provided'}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400 font-medium">System Role</p>
              <span className="inline-block mt-0.5 text-xs font-bold uppercase px-2 py-0.5 rounded-full bg-primary-50 text-primary-700 border border-primary-200">
                {user?.role}
              </span>
            </div>
          </div>
        </div>

        {/* Clinical Modules Roadmap */}
        <div className="md:col-span-2 bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h2 className="text-base font-bold text-slate-900 mb-4 flex items-center justify-between">
            <span>CURA Clinical Modules</span>
            <span className="text-xs font-medium text-slate-400">Milestones 3–8</span>
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {[
              {
                title: 'Appointments & Slots',
                milestone: 'Milestone 3',
                desc: 'Conflict-safe slot booking & doctor discovery',
              },
              {
                title: 'Medical Records (EMR)',
                milestone: 'Milestone 4',
                desc: 'Lab reports, imaging attachments, audit trails',
              },
              {
                title: 'Prescriptions & OCR',
                milestone: 'Milestone 5',
                desc: 'Gemini Vision structured extraction & verification',
              },
              {
                title: 'Medication Reminders',
                milestone: 'Milestone 6',
                desc: 'Scheduled notifications tied to prescriptions',
              },
              {
                title: 'Clinical Triage',
                milestone: 'Milestone 7',
                desc: 'Deterministic red-flag safety rules + AI guidance',
              },
              {
                title: 'Emergency SOS',
                milestone: 'Milestone 8',
                desc: 'PostGIS spatial queries & responder assignment',
              },
            ].map((m) => (
              <div key={m.title} className="p-3.5 rounded-xl border border-slate-100 bg-slate-50/60">
                <div className="flex items-center justify-between mb-1">
                  <p className="text-xs font-bold text-slate-900">{m.title}</p>
                  <span className="text-[10px] font-bold text-primary-600 bg-primary-50 px-2 py-0.5 rounded-full border border-primary-100">
                    {m.milestone}
                  </span>
                </div>
                <p className="text-xs text-slate-500">{m.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
