import { useState } from "react"
import { Link, useLocation } from "react-router-dom"
import { Plus, Sun, Moon, Monitor } from "lucide-react"
import { Button } from "@/components/ui/button"
import { SearchBar } from "./SearchBar"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { useStore } from "@/store/useStore"
import { authService } from "@/services/auth"
import { userService } from "@/services/user"
import { useTheme } from "@/components/theme-provider"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { useQueryClient } from "@tanstack/react-query"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

export default function Navbar() {
  const { currentUser, logout, setAuthModalOpen } = useStore()
  const { theme, setTheme } = useTheme()
  const location = useLocation()
  const queryClient = useQueryClient()
  const [isThemeExpanded, setIsThemeExpanded] = useState(false)
  const [isAvatarModalOpen, setIsAvatarModalOpen] = useState(false)
  const [avatarInput, setAvatarInput] = useState("")
  
  const communityMatch = location.pathname.match(/^\/c\/([^\/]+)/)
  const currentCommunity = communityMatch ? communityMatch[1] : ''

  return (
    <>
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
          <SearchBar />
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
                  <DropdownMenuItem asChild>
                    <Link to={`/u/${currentUser.username}`} className="w-full cursor-pointer">
                      Profile
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => {
                    setAvatarInput(currentUser.avatarUrl || "")
                    setIsAvatarModalOpen(true)
                  }}>
                    Change Avatar
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <div 
                    className="relative flex items-center justify-between px-2 h-9 overflow-hidden cursor-pointer rounded-sm hover:bg-accent hover:text-accent-foreground transition-colors group"
                    onClick={() => setIsThemeExpanded(true)}
                    onMouseEnter={() => setIsThemeExpanded(true)}
                    onMouseLeave={() => setIsThemeExpanded(false)}
                  >
                    <span className={`text-sm transition-all duration-300 ${isThemeExpanded ? '-translate-x-full opacity-0' : 'translate-x-0 opacity-100'}`}>
                      Theme
                    </span>
                    
                    <div className={`absolute right-2 flex items-center text-muted-foreground transition-all duration-300 ${isThemeExpanded ? 'translate-x-full opacity-0' : 'translate-x-0 opacity-100'}`}>
                      {theme === 'light' ? <Sun className="h-4 w-4" /> : theme === 'dark' ? <Moon className="h-4 w-4" /> : <Monitor className="h-4 w-4" />}
                    </div>

                    <div className={`absolute right-1 flex items-center gap-1 rounded-full border bg-background p-0.5 transition-all duration-300 ${isThemeExpanded ? 'translate-x-0 opacity-100 visible' : 'translate-x-4 opacity-0 invisible pointer-events-none'}`}>
                      <Button
                        variant={theme === 'light' ? 'secondary' : 'ghost'}
                        size="icon"
                        className={`h-6 w-6 rounded-full ${theme === 'light' ? 'shadow-sm' : ''}`}
                        onClick={async (e) => {
                          e.stopPropagation()
                          setTheme('light')
                          if (currentUser) {
                            try {
                              const updatedUser = await userService.updateTheme('LIGHT')
                              useStore.setState({ currentUser: updatedUser })
                            } catch (err) { console.error('Failed to sync theme', err) }
                          }
                        }}
                      >
                        <Sun className="h-3 w-3" />
                        <span className="sr-only">Light</span>
                      </Button>
                      <Button
                        variant={theme === 'dark' ? 'secondary' : 'ghost'}
                        size="icon"
                        className={`h-6 w-6 rounded-full ${theme === 'dark' ? 'shadow-sm' : ''}`}
                        onClick={async (e) => {
                          e.stopPropagation()
                          setTheme('dark')
                          if (currentUser) {
                            try {
                              const updatedUser = await userService.updateTheme('DARK')
                              useStore.setState({ currentUser: updatedUser })
                            } catch (err) { console.error('Failed to sync theme', err) }
                          }
                        }}
                      >
                        <Moon className="h-3 w-3" />
                        <span className="sr-only">Dark</span>
                      </Button>
                      <Button
                        variant={theme === 'system' ? 'secondary' : 'ghost'}
                        size="icon"
                        className={`h-6 w-6 rounded-full ${theme === 'system' ? 'shadow-sm' : ''}`}
                        onClick={async (e) => {
                          e.stopPropagation()
                          setTheme('system')
                          if (currentUser) {
                            try {
                              const updatedUser = await userService.updateTheme('SYSTEM')
                              useStore.setState({ currentUser: updatedUser })
                            } catch (err) { console.error('Failed to sync theme', err) }
                          }
                        }}
                      >
                        <Monitor className="h-3 w-3" />
                        <span className="sr-only">System</span>
                      </Button>
                    </div>
                  </div>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={async () => {
                    try {
                      await authService.logout()
                    } catch(e) {
                      console.error("Backend logout failed", e)
                    }
                    logout()
                    queryClient.clear()
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
      {/* Avatar Modal */}
      <Dialog open={isAvatarModalOpen} onOpenChange={setIsAvatarModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Change Avatar</DialogTitle>
            <DialogDescription>
              Enter a new URL for your avatar image.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="avatarUrl">Avatar URL</Label>
              <Input
                id="avatarUrl"
                placeholder="https://example.com/avatar.jpg"
                value={avatarInput}
                onChange={(e) => setAvatarInput(e.target.value)}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsAvatarModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={async () => {
              try {
                const updatedUser = await userService.updateAvatar(avatarInput)
                useStore.setState({ currentUser: updatedUser })
                setIsAvatarModalOpen(false)
              } catch (e) {
                console.error('Failed to update avatar', e)
              }
            }}>
              Save changes
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
