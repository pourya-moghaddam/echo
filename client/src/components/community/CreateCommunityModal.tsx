import { useState } from "react"
import { useMutation, useQueryClient } from "@tanstack/react-query"
import { useNavigate } from "react-router-dom"
import { communityService } from "@/services/community"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
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

interface CreateCommunityModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function CreateCommunityModal({ open, onOpenChange }: CreateCommunityModalProps) {
  const [name, setName] = useState("")
  const [description, setDescription] = useState("")
  const [category, setCategory] = useState("")
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  const createMutation = useMutation({
    mutationFn: async () => {
      return communityService.createCommunity({ name, description, category })
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['joined-communities'] })
      onOpenChange(false)
      setName("")
      setDescription("")
      setCategory("")
      navigate(`/c/${data.name}`)
    }
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!name || !description || !category) return
    createMutation.mutate()
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Start a Community</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 pt-4">
          <div className="space-y-2">
            <Input 
              placeholder="Name (e.g. reactjs)" 
              value={name}
              onChange={(e) => setName(e.target.value.replace(/[^a-zA-Z0-9_]/g, '').toLowerCase())}
              required
              maxLength={20}
            />
            <p className="text-xs text-muted-foreground">
              Community names cannot have spaces or special characters.
            </p>
          </div>
          <div className="space-y-2">
            <Textarea 
              placeholder="Description" 
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              maxLength={500}
            />
          </div>
          <div className="space-y-2">
            <Select value={category} onValueChange={setCategory} required>
              <SelectTrigger>
                <SelectValue placeholder="Category" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="PROGRAMMING">Programming</SelectItem>
                <SelectItem value="TECHNOLOGY">Technology</SelectItem>
                <SelectItem value="SCIENCE">Science</SelectItem>
                <SelectItem value="GAMING">Gaming</SelectItem>
                <SelectItem value="ENTERTAINMENT">Entertainment</SelectItem>
                <SelectItem value="NEWS">News</SelectItem>
                <SelectItem value="SPORTS">Sports</SelectItem>
                <SelectItem value="OTHER">Other</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="flex justify-end pt-4">
            <Button type="submit" disabled={createMutation.isPending || !name || !description || !category}>
              {createMutation.isPending ? 'Creating...' : 'Create'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}
