import { Link, useLocation } from "react-router-dom"
import { Home, Flame, Compass } from "lucide-react"
import { useQuery } from "@tanstack/react-query"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { communityService } from "@/services/community"
import { useStore } from "@/store/useStore"

export default function LeftPanel() {
  const location = useLocation()
  const { currentUser } = useStore()
  
  const navItems = [
    { name: "Home", path: "/", icon: Home },
    { name: "Popular", path: "/popular", icon: Flame },
    { name: "Explore", path: "/explore", icon: Compass },
  ]

  const { data: joinedCommunities } = useQuery({
    queryKey: ['joined-communities'],
    queryFn: () => communityService.getJoinedCommunities(),
    enabled: !!currentUser,
  })

  return (
    <aside className="hidden w-64 flex-col gap-6 md:flex shrink-0">
      <nav className="space-y-1">
        {navItems.map((item) => (
          <Button
            key={item.name}
            variant={location.pathname === item.path ? "secondary" : "ghost"}
            className={cn("w-full justify-start gap-3", location.pathname === item.path && "font-semibold")}
            asChild
          >
            <Link to={item.path}>
              <item.icon className="h-4 w-4" />
              {item.name}
            </Link>
          </Button>
        ))}
      </nav>

      {currentUser && (
        <div className="space-y-3">
          <h4 className="px-4 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            Joined Communities
          </h4>
          <div className="space-y-1">
            {joinedCommunities?.map((community) => (
              <Button key={community.name} variant="ghost" className="w-full justify-start" asChild>
                <Link to={`/c/${community.name}`}>
                  <div className="flex h-6 w-6 items-center justify-center rounded-full bg-muted text-xs font-bold">
                    c/
                  </div>
                  <span className="ml-2 truncate">{community.name}</span>
                </Link>
              </Button>
            ))}
            {joinedCommunities?.length === 0 && (
              <p className="px-4 text-sm text-muted-foreground">You haven't joined any communities yet.</p>
            )}
          </div>
        </div>
      )}
    </aside>
  )
}
