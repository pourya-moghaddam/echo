import { Outlet } from "react-router-dom"
import Navbar from "./Navbar"
import LeftPanel from "./LeftPanel"
import RightPanel from "./RightPanel"

export default function MainLayout() {
  return (
    <div className="min-h-screen bg-background font-sans text-foreground">
      <Navbar />
      <div className="container mx-auto flex gap-6 pt-6 px-4 md:px-8">
        <LeftPanel />
        <main className="flex-1 min-w-0">
          <Outlet />
        </main>
        <RightPanel />
      </div>
    </div>
  )
}
