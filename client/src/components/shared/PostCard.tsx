import { Link } from "react-router-dom"
import { MessageSquare, Share2, MoreHorizontal } from "lucide-react"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { VoteWidget } from "./VoteWidget"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

export interface PostProps {
  id: string
  community: string
  author: string
  timeAgo: string
  title: string
  contentSnippet?: string
  score: number
  commentCount: number
  tags?: string[]
  userVote?: 'up' | 'down' | null
}

export function PostCard({ post, onVote }: { post: PostProps, onVote?: (id: string, dir: 'up' | 'down') => void }) {
  return (
    <Card className="flex flex-row gap-0 py-0 overflow-hidden transition-colors bg-background ring-0 border border-border/40 shadow-none hover:bg-card group cursor-pointer">
      {/* Vote Sidebar */}
      <div className="bg-muted/30 px-2 py-3 w-12 flex flex-col items-center shrink-0 border-r border-border/50">
        <VoteWidget score={post.score} userVote={post.userVote} onVote={(dir) => onVote?.(post.id, dir)} />
      </div>

      {/* Main Content */}
      <div className="flex-1 p-3 md:p-4 min-w-0">
        {/* Header */}
        <div className="flex items-center gap-2 text-xs text-muted-foreground mb-2">
          <Link to={`/c/${post.community}`} className="font-bold text-foreground hover:underline">
            c/{post.community}
          </Link>
          <span>•</span>
          <span>Posted by <Link to={`/u/${post.author}`} className="hover:underline">u/{post.author}</Link></span>
          <span>•</span>
          <span>{post.timeAgo}</span>
        </div>

        {/* Title & Content */}
        <Link to={`/post/${post.id}`} className="block mb-2">
          <h2 className="text-lg font-semibold text-foreground mb-1 leading-snug">{post.title}</h2>
          {post.tags && post.tags.length > 0 && (
            <div className="flex gap-2 mb-2 flex-wrap">
              {post.tags.map(tag => (
                <Badge key={tag} variant="secondary" className="text-[10px] px-1.5 py-0">
                  {tag}
                </Badge>
              ))}
            </div>
          )}
          {post.contentSnippet && (
            <p className="text-sm text-muted-foreground line-clamp-3">
              {post.contentSnippet}
            </p>
          )}
        </Link>

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
