import { useState, useEffect } from "react"
import { Link } from "react-router-dom"
import { MessageSquare, Share2, MoreHorizontal } from "lucide-react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { VoteWidget } from "./VoteWidget"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { postService } from "@/services/post"
import { formatTimeAgo } from "@/lib/utils"

export interface PostProps {
  id: string | number
  communityName: string
  authorUsername: string
  createdAt: string
  title: string
  content: string
  score: number
  commentCount?: number
  userVote?: 'up' | 'down' | null
}

export function PostCard({ post, onVote, hideCommunity, isDetail }: { post: PostProps, onVote?: (id: string, dir: 'up' | 'down') => void, hideCommunity?: boolean, isDetail?: boolean }) {
  const dateStr = formatTimeAgo(post.createdAt)
  
  const [localScore, setLocalScore] = useState(post.score)
  const [localVote, setLocalVote] = useState<'up' | 'down' | null>(post.userVote || null)

  useEffect(() => {
    setLocalScore(post.score)
    setLocalVote(post.userVote || null)
  }, [post.score, post.userVote])

  const handleVoteClick = async (dir: 'up' | 'down') => {
    let newVote: 'up' | 'down' | null = dir
    let scoreChange = 0

    if (localVote === dir) {
      newVote = null
      scoreChange = dir === 'up' ? -1 : 1
    } else if (localVote === 'up' && dir === 'down') {
      scoreChange = -2
    } else if (localVote === 'down' && dir === 'up') {
      scoreChange = 2
    } else {
      scoreChange = dir === 'up' ? 1 : -1
    }

    setLocalVote(newVote)
    setLocalScore(prev => prev + scoreChange)

    try {
      if (onVote) {
        onVote(String(post.id), dir)
      } else {
        const apiDir = newVote ? newVote.toUpperCase() as 'UP'|'DOWN' : 'NONE'
        await postService.votePost(Number(post.id), apiDir)
      }
    } catch (e) {
      setLocalVote(localVote)
      setLocalScore(localScore)
    }
  }

  return (
    <Card className={`flex flex-row gap-0 py-0 overflow-hidden transition-colors bg-background ring-0 border border-border/40 shadow-none ${!isDetail ? 'hover:bg-card cursor-pointer' : ''} group`}>
      {/* Vote Sidebar */}
      <div className="bg-muted/30 px-2 py-3 w-12 flex flex-col items-center shrink-0 border-r border-border/50">
        <VoteWidget score={localScore} userVote={localVote} onVote={handleVoteClick} />
      </div>

      {/* Main Content */}
      <div className="flex-1 p-3 md:p-4 min-w-0">
        {/* Header */}
        <div className="flex items-center gap-2 text-xs text-muted-foreground mb-2">
          {!hideCommunity ? (
            <>
              <Link to={`/c/${post.communityName}`} className="font-bold text-foreground hover:underline">
                c/{post.communityName}
              </Link>
              <span>•</span>
              <span>Posted by <Link to={`/u/${post.authorUsername}`} className="hover:underline">u/{post.authorUsername}</Link></span>
            </>
          ) : (
            <Link to={`/u/${post.authorUsername}`} className="font-bold text-foreground hover:underline">
              u/{post.authorUsername}
            </Link>
          )}
          <span>•</span>
          <span>{dateStr}</span>
        </div>

        {/* Title & Content */}
        {isDetail ? (
          <div className="block mb-2 mt-1">
            <h1 className="text-xl font-bold text-foreground mb-3 leading-snug">{post.title}</h1>
            {post.content && (
              <p className="text-sm text-foreground whitespace-pre-wrap leading-relaxed">
                {post.content}
              </p>
            )}
          </div>
        ) : (
          <Link to={`/post/${post.id}`} className="block mb-2">
            <h2 className="text-lg font-semibold text-foreground mb-1 leading-snug">{post.title}</h2>
            {post.content && (
              <p className="text-sm text-muted-foreground line-clamp-3">
                {post.content}
              </p>
            )}
          </Link>
        )}

        {/* Footer Actions */}
        <div className="flex items-center gap-1 mt-2 -ml-2">
          <Button variant="ghost" size="sm" className="h-8 gap-1.5 text-muted-foreground hover:bg-muted/50">
            <MessageSquare className="h-4 w-4" />
            <span className="text-xs font-medium">{post.commentCount} Comments</span>
          </Button>
          <Button variant="ghost" size="sm" className="h-8 gap-1.5 text-muted-foreground hover:bg-muted/50">
            <Share2 className="h-4 w-4" />
            <span className="text-xs font-medium">Share</span>
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="sm" className="h-8 w-8 p-0 text-muted-foreground hover:bg-muted/50 ml-auto md:ml-0">
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="start">
              <DropdownMenuItem>Save</DropdownMenuItem>
              <DropdownMenuItem>Hide</DropdownMenuItem>
              <DropdownMenuItem className="text-destructive">Report</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </Card>
  )
}
