import { useMutation, useQueryClient } from "@tanstack/react-query"
import { postService } from "@/services/post"

export function useVotePost() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, dir }: { id: string; dir: 'up' | 'down' | 'none' }) => 
      postService.votePost(Number(id), dir === 'none' ? 'NONE' : dir.toUpperCase() as any),
    onMutate: async ({ id, dir }) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['feed'] })
      await queryClient.cancelQueries({ queryKey: ['popular-posts'] })
      await queryClient.cancelQueries({ queryKey: ['community-posts'] })

      const updatePages = (oldData: any) => {
        if (!oldData) return oldData
        return {
          ...oldData,
          pages: oldData.pages.map((page: any) => ({
            ...page,
            content: page.content.map((post: any) => {
              if (String(post.id) === id) {
                let scoreDelta = 0;
                if (post.userVote === 'up') scoreDelta -= 1;
                if (post.userVote === 'down') scoreDelta += 1;
                
                if (dir === 'up') scoreDelta += 1;
                if (dir === 'down') scoreDelta -= 1;

                return {
                  ...post,
                  score: post.score + scoreDelta,
                  userVote: dir === 'none' ? null : dir
                }
              }
              return post
            })
          }))
        }
      }

      const previousFeed = queryClient.getQueryData(['feed'])
      const previousPopular = queryClient.getQueryData(['popular-posts'])
      const previousCommunity = queryClient.getQueriesData({ queryKey: ['community-posts'] })

      queryClient.setQueryData(['feed'], updatePages)
      queryClient.setQueryData(['popular-posts'], updatePages)
      
      // Update all community-posts queries
      previousCommunity.forEach(([queryKey, data]) => {
        queryClient.setQueryData(queryKey, updatePages(data))
      })

      return { previousFeed, previousPopular, previousCommunity }
    },
    onError: (_err, _newVote, context: any) => {
      if (context?.previousFeed) {
        queryClient.setQueryData(['feed'], context.previousFeed)
      }
      if (context?.previousPopular) {
        queryClient.setQueryData(['popular-posts'], context.previousPopular)
      }
      if (context?.previousCommunity) {
        context.previousCommunity.forEach(([queryKey, data]: any) => {
          queryClient.setQueryData(queryKey, data)
        })
      }
    },
  })
}
