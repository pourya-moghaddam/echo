import { useQuery } from "@tanstack/react-query"
import { Link } from "react-router-dom"
import { Compass, Users } from "lucide-react"
import { communityService } from "@/services/community"
import { Skeleton } from "@/components/ui/skeleton"
import { Button } from "@/components/ui/button"

export default function Explore() {
  const { data: communities, isLoading } = useQuery({
    queryKey: ['communities'],
    queryFn: () => communityService.getAllCommunities(),
  })

  // Group communities by category
  const groupedCommunities = communities?.reduce((acc, community) => {
    const category = community.category || 'OTHER'
    if (!acc[category]) acc[category] = []
    acc[category].push(community)
    return acc
  }, {} as Record<string, typeof communities>)

  return (
    <div className="flex-1 p-6 max-w-5xl mx-auto w-full">
      <div className="flex items-center gap-3 mb-8 pb-4 border-b">
        <div className="bg-primary/10 p-3 rounded-full text-primary">
          <Compass className="w-8 h-8" />
        </div>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Explore Communities</h1>
          <p className="text-muted-foreground mt-1">Discover new interests and join vibrant discussions.</p>
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="p-4 border rounded-xl space-y-3">
              <Skeleton className="h-6 w-1/3" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-2/3" />
              <Skeleton className="h-8 w-24 mt-4" />
            </div>
          ))}
        </div>
      ) : (
        <div className="space-y-10">
          {groupedCommunities && Object.entries(groupedCommunities).map(([category, catCommunities]) => (
            <section key={category}>
              <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
                <span className="bg-secondary px-3 py-1 rounded-md text-sm uppercase tracking-widest text-secondary-foreground">{category}</span>
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {catCommunities.map((community) => (
                  <Link 
                    key={community.id} 
                    to={`/c/${community.name}`}
                    className="group block p-5 border rounded-xl hover:border-primary/50 hover:shadow-md transition-all bg-card flex flex-col h-full"
                  >
                    <div className="flex-1">
                      <div className="flex justify-between items-start mb-2">
                        <h3 className="font-bold text-lg group-hover:text-primary transition-colors">
                          e/{community.name}
                        </h3>
                      </div>
                      <p className="text-sm text-muted-foreground line-clamp-3 mb-4">
                        {community.description}
                      </p>
                    </div>
                    
                    <div className="flex items-center justify-between pt-4 border-t mt-auto">
                      <div className="flex items-center text-xs text-muted-foreground gap-1.5">
                        <Users className="w-3.5 h-3.5" />
                        <span>{community.memberCount || 0} members</span>
                      </div>
                      <Button variant="secondary" size="sm" className="rounded-full group-hover:bg-primary group-hover:text-primary-foreground transition-colors">
                        View
                      </Button>
                    </div>
                  </Link>
                ))}
              </div>
            </section>
          ))}
          
          {(!communities || communities.length === 0) && (
            <div className="text-center py-20 text-muted-foreground">
              <Compass className="w-12 h-12 mx-auto mb-4 opacity-20" />
              <p className="text-lg">No communities found. Be the first to create one!</p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
