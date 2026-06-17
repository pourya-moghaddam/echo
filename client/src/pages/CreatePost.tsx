import { useState } from "react"
import { useSearchParams, useNavigate } from "react-router-dom"
import { useMutation, useQuery } from "@tanstack/react-query"
import { postService } from "@/services/post"
import { communityService } from "@/services/community"
import { useStore } from "@/store/useStore"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

export default function CreatePost() {
  const [searchParams] = useSearchParams()
  const defaultCommunity = searchParams.get("c") || ""
  
  const [title, setTitle] = useState("")
  const [content, setContent] = useState("")
  const [communityName, setCommunityName] = useState(defaultCommunity)

  const { currentUser } = useStore()
  const navigate = useNavigate()

  // Fetch joined communities to populate the selector
  const { data: joinedCommunities } = useQuery({
    queryKey: ['joined-communities'],
    queryFn: () => communityService.getJoinedCommunities(),
    enabled: !!currentUser,
  })

  // If we have a default community from URL but it's not in joined communities (or we haven't loaded yet),
  // we still want to be able to show it as the selected value or at least allow it. 
  // However, normally users can only post to joined communities or all communities. 
  // For simplicity, we just use the defaultCommunity as initial state.

  const createPostMutation = useMutation({
    mutationFn: async () => {
      return postService.createPost({ title, content, communityName })
    },
    onSuccess: (data) => {
      // Redirect to the newly created post or the community page
      navigate(`/c/${data.communityName}`)
    }
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!title || !content || !communityName) return
    createPostMutation.mutate()
  }

  if (!currentUser) {
    return (
      <div className="flex justify-center py-12">
        <p className="text-muted-foreground">Please log in to create a post.</p>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto py-8 px-4">
      <h1 className="text-2xl font-bold mb-6">Create a Post</h1>
      
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="space-y-2">
          <Select 
            value={communityName} 
            onValueChange={setCommunityName}
            required
          >
            <SelectTrigger className="w-[280px]">
              <SelectValue placeholder="Choose a community" />
            </SelectTrigger>
            <SelectContent>
              {defaultCommunity && !joinedCommunities?.some(c => c.name === defaultCommunity) && (
                <SelectItem value={defaultCommunity}>c/{defaultCommunity}</SelectItem>
              )}
              {joinedCommunities?.map((c) => (
                <SelectItem key={c.name} value={c.name}>
                  c/{c.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Input 
            placeholder="Title" 
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            maxLength={300}
            className="text-lg font-medium"
          />
        </div>

        <div className="space-y-2">
          <Textarea 
            placeholder="Text (optional)" 
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            className="min-h-[200px] resize-y"
          />
        </div>

        <div className="flex justify-end gap-2">
          <Button 
            type="button" 
            variant="outline" 
            onClick={() => navigate(-1)}
          >
            Cancel
          </Button>
          <Button 
            type="submit" 
            disabled={!title || !content || !communityName || createPostMutation.isPending}
          >
            {createPostMutation.isPending ? 'Posting...' : 'Post'}
          </Button>
        </div>
      </form>
    </div>
  )
}
