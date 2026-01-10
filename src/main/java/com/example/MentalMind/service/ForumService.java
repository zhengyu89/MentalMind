package com.example.MentalMind.service;

import com.example.MentalMind.model.ForumComment;
import com.example.MentalMind.model.ForumPost;
import com.example.MentalMind.model.User;
import com.example.MentalMind.model.ForumPostLike;
import com.example.MentalMind.model.ForumPostFlag;
import com.example.MentalMind.repository.ForumCommentRepository;
import com.example.MentalMind.repository.ForumPostRepository;
import com.example.MentalMind.repository.ForumPostLikeRepository;
import com.example.MentalMind.repository.ForumPostFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ForumService {

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private ForumPostLikeRepository forumPostLikeRepository;

    @Autowired
    private ForumPostFlagRepository forumPostFlagRepository;

    /**
     * Create a new forum post
     */
    @Transactional
    public ForumPost createPost(User user, String title, String content, String category, boolean anonymous) {
        ForumPost post = new ForumPost(user, title, content, category, anonymous);
        post.setStatus("PENDING"); // All posts start as pending for moderation
        return forumPostRepository.save(post);
    }

    /**
     * Get all approved posts (for students)
     */
    public List<ForumPost> getApprovedPosts() {
        return forumPostRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
    }

    /**
     * Get approved posts by category
     */
    public List<ForumPost> getPostsByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("all")) {
            return getApprovedPosts();
        }
        return forumPostRepository.findByCategoryAndApproved(category.toLowerCase());
    }

    /**
     * Get posts that need moderation (PENDING with flags or PENDING status)
     * Returns PENDING posts only; flagged posts are identified by flagCount > 0
     */
    public List<ForumPost> getPostsForModeration() {
        return forumPostRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    /**
     * Get approved posts with flags (flagged content)
     */
    public List<ForumPost> getFlaggedPosts() {
        List<ForumPost> approved = getApprovedPosts();
        return approved.stream()
            .filter(post -> post.getFlagCount() > 0)
            .toList();
    }

    /**
     * Get a post by ID
     */
    public Optional<ForumPost> getPostById(Long id) {
        return forumPostRepository.findById(id);
    }

    /**
     * Toggle like on a post for a specific user. Returns result with like count and liked state.
     */
    @Transactional
    public LikeToggleResult toggleLike(Long postId, User user) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean alreadyLiked = forumPostLikeRepository.existsByPostAndUser(post, user);

        if (alreadyLiked) {
            forumPostLikeRepository.deleteByPostAndUser(post, user);
        } else {
            ForumPostLike like = new ForumPostLike(post, user);
            forumPostLikeRepository.save(like);
        }

        // Sync post likeCount from actual count for consistency
        long count = forumPostLikeRepository.countByPost(post);
        post.setLikeCount((int) count);
        post.setUpdatedAt(LocalDateTime.now());
        forumPostRepository.save(post);

        return new LikeToggleResult((int) count, !alreadyLiked);
    }

    public static class LikeToggleResult {
        private final int likeCount;
        private final boolean liked;

        public LikeToggleResult(int likeCount, boolean liked) {
            this.likeCount = likeCount;
            this.liked = liked;
        }

        public int getLikeCount() { return likeCount; }
        public boolean isLiked() { return liked; }
    }

    /**
     * Add a comment to a post
     */
    @Transactional
    public ForumComment addComment(Long postId, User user, String content, boolean anonymous) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        ForumComment comment = new ForumComment(post, user, content, anonymous);
        return forumCommentRepository.save(comment);
    }

    /**
     * Get all comments for a post
     */
    public List<ForumComment> getPostComments(Long postId) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        return forumCommentRepository.findByPostOrderByCreatedAtAsc(post);
    }

    /**
     * Approve a post (counselor moderation)
     */
    @Transactional
    public ForumPost approvePost(Long postId, String moderationNote) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        post.setStatus("APPROVED");
        post.setModerationNote(moderationNote);
        post.setUpdatedAt(LocalDateTime.now());
        return forumPostRepository.save(post);
    }

    /**
     * Reject a post (counselor moderation)
     */
    @Transactional
    public ForumPost rejectPost(Long postId, String moderationNote) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        post.setStatus("REJECTED");
        post.setModerationNote(moderationNote);
        post.setUpdatedAt(LocalDateTime.now());
        return forumPostRepository.save(post);
    }

    /**
     * Flag a post with a reason (multiple flags can exist per post)
     * Post remains visible to public, but counselor can see flags and decide to delete
     */
    @Transactional
    public ForumPost flagPost(Long postId, User user, String reason) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Check if user already flagged this post
        if (forumPostFlagRepository.existsByPostAndUser(post, user)) {
            throw new RuntimeException("You have already reported this post");
        }
        
        ForumPostFlag flag = new ForumPostFlag(post, user, reason);
        forumPostFlagRepository.save(flag);
        
        // Update flag count
        long newCount = forumPostFlagRepository.countByPost(post);
        post.setFlagCount((int) newCount);
        post.setUpdatedAt(LocalDateTime.now());
        return forumPostRepository.save(post);
    }

    /**
     * Clear all flags from a post (when counselor approves it)
     */
    @Transactional
    public ForumPost clearFlags(Long postId) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        forumPostFlagRepository.deleteByPost(post);
        post.setFlagCount(0);
        post.setUpdatedAt(LocalDateTime.now());
        return forumPostRepository.save(post);
    }

    /**
     * Get all flags for a post
     */
    public List<ForumPostFlag> getPostFlags(Long postId) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        return forumPostFlagRepository.findByPostOrderByCreatedAtDesc(post);
    }

    /**
     * Check if a user has already flagged a post
     */
    public boolean hasUserFlaggedPost(ForumPost post, User user) {
        return forumPostFlagRepository.existsByPostAndUser(post, user);
    }

    /**
     * Get posts by user
     */
    public List<ForumPost> getUserPosts(User user) {
        return forumPostRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Count pending posts
     */
    public long countPendingPosts() {
        return forumPostRepository.countByStatus("PENDING");
    }

    /**
     * Count flagged posts (posts with flagCount > 0)
     */
    public long countFlaggedPosts() {
        return getFlaggedPosts().size();
    }

    /**
     * Set post to pending status (counselor moderation)
     */
    @Transactional
    public ForumPost setPendingPost(Long postId, String moderationNote) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        post.setStatus("PENDING");
        post.setModerationNote(moderationNote);
        post.setUpdatedAt(LocalDateTime.now());
        return forumPostRepository.save(post);
    }

    /**
     * Delete a post (counselor moderation)
     */
    @Transactional
    public void deletePost(Long postId) {
        ForumPost post = forumPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Delete all likes, comments, and flags first (cascade should handle this, but being explicit)
        forumPostLikeRepository.deleteByPost(post);
        forumCommentRepository.deleteByPost(post);
        forumPostFlagRepository.deleteByPost(post);
        forumPostRepository.delete(post);
    }
}
