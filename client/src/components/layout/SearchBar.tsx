import { useState, useRef, useEffect } from "react"
import { Search } from "lucide-react"
import { useNavigate } from "react-router-dom"
import { useQuery } from "@tanstack/react-query"
import { Input } from "@/components/ui/input"
import { searchService } from "@/services/search"
import { useDebounce } from "@/hooks/useDebounce"

export function SearchBar() {
  const [query, setQuery] = useState("")
  const [isFocused, setIsFocused] = useState(false)
  const debouncedQuery = useDebounce(query, 300)
  const navigate = useNavigate()
  const containerRef = useRef<HTMLDivElement>(null)

  const { data: communities } = useQuery({
    queryKey: ['search', 'communities', debouncedQuery],
    queryFn: () => searchService.searchCommunities(debouncedQuery),
    enabled: debouncedQuery.length > 0,
  })

  const { data: posts } = useQuery({
    queryKey: ['search', 'posts', debouncedQuery],
    queryFn: () => searchService.searchPosts(debouncedQuery),
    enabled: debouncedQuery.length > 0,
  })

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsFocused(false)
      }
    }
    document.addEventListener("mousedown", handleClickOutside)
    return () => document.removeEventListener("mousedown", handleClickOutside)
  }, [])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (query.trim()) {
      setIsFocused(false)
      navigate(`/search?q=${encodeURIComponent(query.trim())}`)
    }
  }

  const isLoading = (debouncedQuery.length > 0 && !communities && !posts)
  const hasResults = (communities && communities.length > 0) || (posts && posts.length > 0)
  const showDropdown = isFocused && debouncedQuery.length > 0

  return (
    <div className="relative w-full max-w-md md:w-[400px]" ref={containerRef}>
      <form onSubmit={handleSubmit}>
        <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <Input
          type="search"
          placeholder="Search Echo..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setIsFocused(true)}
          className="w-full bg-secondary dark:bg-secondary border-transparent hover:bg-secondary/80 dark:hover:bg-secondary/80 focus-visible:bg-background dark:focus-visible:bg-background focus-visible:border-ring pl-9 pr-4 transition-colors"
        />
      </form>

      {showDropdown && (
        <div className="absolute top-full left-0 right-0 mt-1 rounded-md border bg-popover text-popover-foreground shadow-md z-50 max-h-[80vh] overflow-y-auto">
          {!isLoading && !hasResults ? (
            <div className="p-4 text-center text-sm text-muted-foreground">
              No results found.
            </div>
          ) : (
            <>
              {communities && communities.length > 0 && (
                <div className="p-2 border-b">
                  <h3 className="text-xs font-semibold text-muted-foreground mb-2 px-2 uppercase tracking-wider">Communities</h3>
                  <div className="space-y-1">
                    {communities.slice(0, 3).map((c) => (
                      <button
                        key={c.id}
                        onClick={() => {
                          setIsFocused(false)
                          navigate(`/c/${c.name}`)
                        }}
                        className="w-full text-left px-2 py-1.5 text-sm rounded-sm hover:bg-accent hover:text-accent-foreground flex items-center gap-2"
                      >
                        <span className="font-medium">e/{c.name}</span>
                        <span className="text-xs text-muted-foreground truncate">{c.category}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {posts && posts.length > 0 && (
                <div className="p-2">
                  <h3 className="text-xs font-semibold text-muted-foreground mb-2 px-2 uppercase tracking-wider">Posts</h3>
                  <div className="space-y-1">
                    {posts.slice(0, 5).map((p) => (
                      <button
                        key={p.id}
                        onClick={() => {
                          setIsFocused(false)
                          navigate(`/post/${p.id}`)
                        }}
                        className="w-full text-left px-2 py-1.5 text-sm rounded-sm hover:bg-accent hover:text-accent-foreground flex flex-col"
                      >
                        <span className="font-medium truncate">{p.title}</span>
                        <span className="text-xs text-muted-foreground truncate">e/{p.communityName} • u/{p.authorUsername}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}
