import { Outlet } from "react-router-dom"
import Navbar from "./Navbar"

export default function MainLayout() {
  return (
    <div className="min-h-screen bg-background font-sans text-foreground">
      <Navbar />
      <div className="container mx-auto flex gap-6 pt-6">
        {/* Left Panel */}
        <aside className="hidden w-64 flex-col gap-4 md:flex">
          <div className="rounded-lg border bg-card p-4 shadow-sm">
            <h3 className="font-semibold mb-2 text-sm">Navigation</h3>
            <ul className="space-y-1 text-sm text-muted-foreground">
              <li className="hover:text-foreground cursor-pointer transition-colors">Home</li>
              <li className="hover:text-foreground cursor-pointer transition-colors">Popular</li>
            </ul>
          </div>
        </aside>

        {/* Middle Content Area */}
        <main className="flex-1">
          <Outlet />
        </main>

        {/* Right Panel (Conditional, but we'll leave a placeholder structure) */}
        <aside className="hidden w-80 flex-col gap-4 lg:flex">
          <div className="rounded-lg border bg-card p-4 shadow-sm">
            <h3 className="font-semibold mb-2 text-sm">Community Info</h3>
            <p className="text-xs text-muted-foreground">Select a community to view info.</p>
          </div>
        </aside>
      </div>
    </div>
  )
}
