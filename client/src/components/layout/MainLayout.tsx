import { Outlet } from "react-router-dom"
import Navbar from "./Navbar"
import LeftPanel from "./LeftPanel"
import RightPanel from "./RightPanel"
import { AuthModal } from "../auth/AuthModal"

export default function MainLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-background text-foreground">
      <Navbar />
      <div className="container mx-auto flex flex-1 gap-6 px-4 py-6 md:px-8">
        <LeftPanel />
        <main className="flex-1 min-w-0">
          <Outlet />
        </main>
        <RightPanel />
      </div>
      <AuthModal />
    </div>
  )
}
