import { useLocation } from "react-router-dom"
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { Button } from "@/components/ui/button"
import { communityService } from "@/services/community"
import { useStore } from "@/store/useStore"

export default function RightPanel() {
  const location = useLocation()
  const { currentUser } = useStore()
  const queryClient = useQueryClient()

  const isCommunityPage = location.pathname.startsWith('/c/')
  const communityName = isCommunityPage ? location.pathname.split('/')[2] : ''

  const { data: community, isLoading } = useQuery({
    queryKey: ['community', communityName],
    queryFn: () => communityService.getCommunity(communityName),
    enabled: !!communityName && isCommunityPage,
  })

  const { data: joinedCommunities } = useQuery({
    queryKey: ['joined-communities'],
    queryFn: () => communityService.getJoinedCommunities(),
    enabled: !!currentUser,
  })

  const isJoined = joinedCommunities?.some(c => c.name.toLowerCase() === communityName.toLowerCase())

  const toggleJoinMutation = useMutation({
    mutationFn: async () => {
      if (isJoined) {
        await communityService.leaveCommunity(communityName)
      } else {
        await communityService.joinCommunity(communityName)
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['community', communityName] })
      queryClient.invalidateQueries({ queryKey: ['joined-communities'] })
    }
  })

  // Only show the right panel on community pages
  if (!isCommunityPage) {
    return <aside className="hidden w-80 lg:block shrink-0"></aside>
  }

  return (
    <aside className="hidden w-80 flex-col gap-4 lg:flex shrink-0">
      <div className="rounded-lg border bg-card text-card-foreground shadow-sm">
        <div className="p-4 flex flex-col gap-3">
          <h3 className="font-semibold text-lg truncate">c/{communityName}</h3>
          
          {isLoading ? (
            <div className="text-sm text-muted-foreground">Loading...</div>
          ) : community ? (
            <>
              <p className="text-sm text-muted-foreground">
                {community.description}
              </p>
              <div className="flex items-center gap-4 text-sm mt-2">
                <div>
                  <div className="font-semibold">{community.memberCount}</div>
                  <div className="text-muted-foreground text-xs">Members</div>
                </div>
                {community.createdAt && (
                  <div>
                    <div className="font-semibold">
                      {new Date(community.createdAt).toLocaleDateString('en-US', {
                        month: 'long',
                        day: 'numeric',
                        year: 'numeric'
                      })}
                    </div>
                    <div className="text-muted-foreground text-xs">Created</div>
                  </div>
                )}
              </div>
              
              {currentUser && (
                <Button 
                  className="w-full mt-2"
                  variant={isJoined ? "outline" : "default"}
                  onClick={() => toggleJoinMutation.mutate()}
                  disabled={toggleJoinMutation.isPending}
                >
                  {isJoined ? "Leave Community" : "Join Community"}
                </Button>
              )}
            </>
          ) : (
            <div className="text-sm text-muted-foreground">Community not found</div>
          )}
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
