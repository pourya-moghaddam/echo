import { Link, useLocation } from "react-router-dom"
import { Search, Plus } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { useStore } from "@/store/useStore"
import { authService } from "@/services/auth"
import { userService } from "@/services/user"
import { useTheme } from "@/components/theme-provider"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

export default function Navbar() {
  const { currentUser, logout, setAuthModalOpen } = useStore()
  const { theme, setTheme } = useTheme()
  const location = useLocation()
  
  const communityMatch = location.pathname.match(/^\/c\/([^\/]+)/)
  const currentCommunity = communityMatch ? communityMatch[1] : ''

  return (
    <nav className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto flex h-14 items-center justify-between px-4 md:px-8">
        {/* Logo */}
        <div className="flex items-center gap-2">
          <Link to="/" className="text-3xl font-bold tracking-tighter text-foreground" style={{ fontFamily: "'Space Grotesk', sans-serif" }}>
            echo.
          </Link>
        </div>

        {/* Search Bar */}
        <div className="flex flex-1 items-center justify-center px-6">
          <div className="relative w-full max-w-md">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              type="search"
              placeholder="Search Echo..."
              className="w-full bg-secondary dark:bg-secondary border-transparent hover:bg-secondary/80 dark:hover:bg-secondary/80 focus-visible:bg-background dark:focus-visible:bg-background focus-visible:border-ring pl-9 pr-4 md:w-[400px] transition-colors"
            />
          </div>
        </div>

        {/* Right Actions */}
        <div className="flex items-center gap-2">
          <div className="hidden items-center gap-4 md:flex">
            {currentUser ? (
              <>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <Button variant="ghost" size="icon" asChild>
                      <Link to={`/submit${currentCommunity ? `?c=${currentCommunity}` : ''}`}>
                        <Plus className="h-5 w-5" />
                        <span className="sr-only">Create Post</span>
                      </Link>
                    </Button>
                  </TooltipTrigger>
                  <TooltipContent>
                    <p>Create Post</p>
                  </TooltipContent>
                </Tooltip>
                <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Avatar className="h-8 w-8 cursor-pointer outline-none border-none ring-0">
                    <AvatarImage src={currentUser.avatarUrl} />
                    <AvatarFallback>{currentUser.username[0].toUpperCase()}</AvatarFallback>
                  </Avatar>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <div className="flex items-center justify-start gap-2 p-2">
                    <div className="flex flex-col space-y-1 leading-none">
                      <p className="font-medium">{currentUser.username}</p>
                    </div>
                  </div>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={async () => {
                    const nextTheme = theme === 'dark' ? 'light' : theme === 'light' ? 'system' : 'dark'
                    setTheme(nextTheme)
                    if (currentUser) {
                      try {
                        const updatedUser = await userService.updateTheme(nextTheme.toUpperCase() as any)
                        useStore.setState({ currentUser: updatedUser })
                      } catch (e) { console.error('Failed to sync theme', e) }
                    }
                  }}>
                    Theme: {theme.charAt(0).toUpperCase() + theme.slice(1)}
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={async () => {
                    const url = window.prompt("Enter new avatar URL:", currentUser.avatarUrl)
                    if (url) {
                      try {
                        const updatedUser = await userService.updateAvatar(url)
                        useStore.setState({ currentUser: updatedUser })
                      } catch (e) { console.error('Failed to update avatar', e) }
                    }
                  }}>
                    Change Avatar
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={async () => {
                    try {
                      await authService.logout()
                    } catch(e) {
                      console.error("Backend logout failed", e)
                    }
                    logout()
                  }} className="text-destructive cursor-pointer">
                    Log out
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
              </>
            ) : (
              <>
                <Button variant="ghost" onClick={() => setAuthModalOpen(true, 'login')}>Log in</Button>
                <Button onClick={() => setAuthModalOpen(true, 'signup')}>Sign up</Button>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
