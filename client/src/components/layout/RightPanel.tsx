import { useLocation } from "react-router-dom"
import { Button } from "@/components/ui/button"

export default function RightPanel() {
  const location = useLocation()

  // Only show the right panel on community pages
  if (!location.pathname.startsWith('/c/')) {
    return <aside className="hidden w-80 lg:block shrink-0"></aside>
  }

  // Placeholder logic, would fetch actual data later
  const communityName = location.pathname.split('/')[2] || "community"

  return (
    <aside className="hidden w-80 flex-col gap-4 lg:flex shrink-0">
      <div className="rounded-lg border bg-card text-card-foreground shadow-sm">
        <div className="p-4 flex flex-col gap-3">
          <h3 className="font-semibold text-lg truncate">c/{communityName}</h3>
          <p className="text-sm text-muted-foreground">
            A community dedicated to all things {communityName}. Join the discussion!
          </p>
          <div className="flex items-center gap-4 text-sm mt-2">
            <div>
              <div className="font-semibold">350k</div>
              <div className="text-muted-foreground text-xs">Members</div>
            </div>
            <div>
              <div className="font-semibold">1.2k</div>
              <div className="text-muted-foreground text-xs">Online</div>
            </div>
          </div>
          <Button className="w-full mt-2">Join Community</Button>
        </div>
      </div>

      <div className="rounded-lg border bg-card text-card-foreground shadow-sm p-4 text-sm">
        <h4 className="font-semibold mb-2">Rules</h4>
        <ol className="list-decimal list-inside space-y-1 text-muted-foreground">
          <li>Be respectful</li>
          <li>No spam or self-promotion</li>
          <li>Keep posts relevant</li>
        </ol>
      </div>
    </aside>
  )
}
