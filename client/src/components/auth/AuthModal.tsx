import { useState, useEffect } from "react"
import { Eye, EyeOff } from "lucide-react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useStore } from "@/store/useStore"
import { authService } from "@/services/auth"
import { useTheme } from "@/components/theme-provider"
import { useQueryClient } from "@tanstack/react-query"

export function AuthModal() {
  const { isAuthModalOpen, setAuthModalOpen, authModalMode, login } = useStore()
  const { setTheme } = useTheme()
  const queryClient = useQueryClient()
  const isLoginMode = authModalMode === 'login'
  const [isLoading, setIsLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [username, setUsername] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")

  useEffect(() => {
    if (!isAuthModalOpen) {
      setUsername("")
      setEmail("")
      setPassword("")
      setError("")
      setShowPassword(false)
    }
  }, [isAuthModalOpen])

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    if (username.length < 3) return setError("Username must be at least 3 characters")
    if (password.length < 6) return setError("Password must be at least 6 characters")
    if (!isLoginMode && !email.includes('@')) return setError("Please enter a valid email")
    
    setIsLoading(true)
    try {
      const response = isLoginMode 
        ? await authService.login({ usernameOrEmail: username, password })
        : await authService.signup({ email, username, password })
        
      login(response.user, response.token)
      queryClient.clear()
      if (response.user.themePreference) {
        setTheme(response.user.themePreference.toLowerCase() as any)
      }
    } catch (err: any) {
      console.error("Auth failed:", err)
      setError(err.response?.data?.message || "An error occurred during authentication.")
    } finally {
      setIsLoading(false)
    }
  }

  const toggleMode = () => {
    setAuthModalOpen(true, isLoginMode ? 'signup' : 'login')
    setError("")
    setUsername("")
    setEmail("")
    setPassword("")
    setShowPassword(false)
  }

  return (
    <Dialog open={isAuthModalOpen} onOpenChange={setAuthModalOpen}>
      <DialogContent className="sm:max-w-[400px] outline-none focus:outline-none focus-visible:outline-none focus:ring-0">
        <DialogHeader>
          <DialogTitle>{isLoginMode ? "Welcome back to echo." : "Create an account"}</DialogTitle>
          <DialogDescription>
            {isLoginMode ? "Enter your credentials to access your account." : "Sign up to start sharing and voting."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className="space-y-4 py-4">
          {!isLoginMode && (
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input 
                id="email" 
                type="email"
                placeholder="Enter email" 
                value={email} 
                onChange={e => setEmail(e.target.value)} 
                disabled={isLoading} 
              />
            </div>
          )}
          
          <div className="space-y-2">
            <Label htmlFor="username">{isLoginMode ? "Username or Email" : "Username"}</Label>
            <Input 
              id="username" 
              placeholder={isLoginMode ? "Enter username or email" : "Enter username"} 
              value={username} 
              onChange={e => setUsername(e.target.value)} 
              disabled={isLoading} 
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <div className="relative">
              <Input 
                id="password" 
                type={showPassword ? "text" : "password"} 
                placeholder="Enter password" 
                value={password} 
                onChange={e => setPassword(e.target.value)} 
                disabled={isLoading} 
                className="pr-10"
              />
              <button 
                type="button" 
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                tabIndex={-1}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          {error && <p className="text-sm text-destructive">{error}</p>}
          
          <Button type="submit" className="w-full" disabled={isLoading}>
            {isLoading ? "Please wait..." : (isLoginMode ? "Log in" : "Sign up")}
          </Button>
        </form>

        <div className="text-center text-sm text-muted-foreground mt-2">
          {isLoginMode ? "Don't have an account? " : "Already have an account? "}
          <button 
            type="button" 
            className="text-foreground font-semibold hover:underline"
            onClick={toggleMode}
            disabled={isLoading}
          >
            {isLoginMode ? "Sign up" : "Log in"}
          </button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
