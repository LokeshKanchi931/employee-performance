package com.springboot.employeeperformance.repository;

import com.springboot.employeeperformance.dto.Requests;
import com.springboot.employeeperformance.dto.Responses;
import com.springboot.employeeperformance.entity.*;
import com.springboot.employeeperformance.exception.ResourceNotFoundException;
import com.springboot.employeeperformance.mapper.EmployeeMapper;
import com.springboot.employeeperformance.mapper.ReviewMapper;
import com.springboot.employeeperformance.service.ReviewService;
import com.springboot.employeeperformance.service.interfaces.IEmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock PerformanceReviewRepository reviewRepository;
    @Mock ReviewCycleRepository       cycleRepository;
    @Mock GoalRepository              goalRepository;
    @Mock IEmployeeService            employeeService;
    @Mock ReviewMapper                reviewMapper;
    @Mock EmployeeMapper              employeeMapper;
    @InjectMocks ReviewService reviewService;

    private Employee alice;
    private Employee david;
    private ReviewCycle openCycle;
    private ReviewCycle closedCycle;
    private PerformanceReview savedReview;
    private Responses.ReviewWithCycle reviewDto;
    private Pageable defaultPageable;

    @BeforeEach
    void setUp() {
        alice = Employee.builder()
                .id(1L).name("Alice").department("Engineering")
                .role("SWE").joiningDate(LocalDate.of(2022, 1, 1))
                .isActive(true).build();

        david = Employee.builder()
                .id(4L).name("David").department("Engineering")
                .role("Tech Lead").joiningDate(LocalDate.of(2019, 1, 1))
                .isActive(true).build();

        openCycle = ReviewCycle.builder()
                .id(1L).name("Q1 2025")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 3, 31))
                .status(ReviewCycle.Status.open).build();

        closedCycle = ReviewCycle.builder()
                .id(2L).name("Q4 2024")
                .startDate(LocalDate.of(2024, 10, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .status(ReviewCycle.Status.closed).build();

        savedReview = PerformanceReview.builder()
                .id(1L).employee(alice).cycle(openCycle)
                .reviewer(david).reviewType(PerformanceReview.ReviewType.manager)
                .rating((short) 4).build();

        reviewDto = Responses.ReviewWithCycle.builder()
                .reviewId(1L).rating((short) 4).reviewType("manager").build();

        defaultPageable = PageRequest.of(0, 10);
    }

    // ── submitReview ──────────────────────────────────────────

    @Nested
    @DisplayName("submitReview")
    class SubmitReview {

        @Test
        @DisplayName("saves review and returns mapped DTO")
        void savesAndReturnsDto() {
            var request = reviewRequest(1L, 1L, 4L, (short) 4);
            when(employeeService.getOrThrow(1L)).thenReturn(alice);
            when(employeeService.getOrThrow(4L)).thenReturn(david);
            when(cycleRepository.findById(1L)).thenReturn(Optional.of(openCycle));
            when(reviewRepository.save(any())).thenReturn(savedReview);
            when(reviewMapper.toReviewWithCycle(savedReview)).thenReturn(reviewDto);

            Responses.ReviewWithCycle result = reviewService.submitReview(request);

            assertThat(result.getReviewId()).isEqualTo(1L);
            assertThat(result.getRating()).isEqualTo((short) 4);
            verify(reviewRepository).save(any(PerformanceReview.class));
        }

        @Test
        @DisplayName("throws when cycle is closed")
        void throwsWhenCycleIsClosed() {
            var request = reviewRequest(1L, 2L, null, (short) 4);
            when(employeeService.getOrThrow(1L)).thenReturn(alice);
            when(cycleRepository.findById(2L)).thenReturn(Optional.of(closedCycle));

            assertThatThrownBy(() -> reviewService.submitReview(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("closed cycle");

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when employee not found")
        void throwsWhenEmployeeNotFound() {
            var request = reviewRequest(99L, 1L, null, (short) 4);
            when(employeeService.getOrThrow(99L))
                    .thenThrow(new ResourceNotFoundException("Employee not found: 99"));

            assertThatThrownBy(() -> reviewService.submitReview(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("throws when cycle not found")
        void throwsWhenCycleNotFound() {
            var request = reviewRequest(1L, 99L, null, (short) 4);
            when(employeeService.getOrThrow(1L)).thenReturn(alice);
            when(cycleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.submitReview(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("reviewer is null when reviewerId not provided")
        void reviewerIsNullWhenNotProvided() {
            var request = reviewRequest(1L, 1L, null, (short) 4);
            when(employeeService.getOrThrow(1L)).thenReturn(alice);
            when(cycleRepository.findById(1L)).thenReturn(Optional.of(openCycle));
            when(reviewRepository.save(any())).thenReturn(savedReview);
            when(reviewMapper.toReviewWithCycle(any())).thenReturn(reviewDto);

            reviewService.submitReview(request);

            verify(employeeService, times(1)).getOrThrow(any());
            verify(reviewRepository).save(argThat(r -> r.getReviewer() == null));
        }

        @Test
        @DisplayName("defaults reviewType to manager when not provided")
        void defaultsReviewTypeToManager() {
            var request = reviewRequest(1L, 1L, null, (short) 4);
            request.setReviewType(null);
            when(employeeService.getOrThrow(1L)).thenReturn(alice);
            when(cycleRepository.findById(1L)).thenReturn(Optional.of(openCycle));
            when(reviewRepository.save(any())).thenReturn(savedReview);
            when(reviewMapper.toReviewWithCycle(any())).thenReturn(reviewDto);

            reviewService.submitReview(request);

            verify(reviewRepository).save(argThat(r ->
                    r.getReviewType() == PerformanceReview.ReviewType.manager));
        }
    }

    // ── getReviewsForEmployee (paginated) ─────────────────────

    @Nested
    @DisplayName("getReviewsForEmployee")
    class GetReviewsForEmployee {

        @Test
        @DisplayName("returns paged reviews for existing employee")
        void returnsMappedPagedReviews() {
            var pageResult = new PageImpl<>(List.of(savedReview), defaultPageable, 1);
            when(employeeService.getOrThrow(1L)).thenReturn(alice);
            when(reviewRepository.findByEmployeeIdWithCycleAndReviewer(1L, defaultPageable))
                    .thenReturn(pageResult);
            when(reviewMapper.toReviewWithCycle(savedReview)).thenReturn(reviewDto);

            Responses.PagedResponse<Responses.ReviewWithCycle> result =
                    reviewService.getReviewsForEmployee(1L, defaultPageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getReviewId()).isEqualTo(1L);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty paged response when no reviews")
        void returnsEmptyPagedResponse() {
            var emptyPage = new PageImpl<PerformanceReview>(List.of(), defaultPageable, 0);
            when(employeeService.getOrThrow(1L)).thenReturn(alice);
            when(reviewRepository.findByEmployeeIdWithCycleAndReviewer(1L, defaultPageable))
                    .thenReturn(emptyPage);

            Responses.PagedResponse<Responses.ReviewWithCycle> result =
                    reviewService.getReviewsForEmployee(1L, defaultPageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("page metadata reflects correct page number and size")
        void pageMetadataIsCorrect() {
            Pageable pageable = PageRequest.of(2, 5);
            var pageResult = new PageImpl<>(List.of(savedReview), pageable, 11);
            when(employeeService.getOrThrow(1L)).thenReturn(alice);
            when(reviewRepository.findByEmployeeIdWithCycleAndReviewer(1L, pageable))
                    .thenReturn(pageResult);
            when(reviewMapper.toReviewWithCycle(any())).thenReturn(reviewDto);

            Responses.PagedResponse<Responses.ReviewWithCycle> result =
                    reviewService.getReviewsForEmployee(1L, pageable);

            assertThat(result.getPage()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(11);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.isLast()).isFalse();
        }

        @Test
        @DisplayName("throws when employee not found")
        void throwsWhenEmployeeNotFound() {
            when(employeeService.getOrThrow(99L))
                    .thenThrow(new ResourceNotFoundException("Employee not found: 99"));

            assertThatThrownBy(() -> reviewService.getReviewsForEmployee(99L, defaultPageable))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── getCycleSummary ───────────────────────────────────────

    @Nested
    @DisplayName("getCycleSummary")
    class GetCycleSummary {

        @Test
        @DisplayName("throws when cycle not found")
        void throwsWhenCycleNotFound() {
            when(cycleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.getCycleSummary(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("rounds average rating to 2 decimal places")
        void roundsAverageRating() {
            when(cycleRepository.findById(1L)).thenReturn(Optional.of(openCycle));
            when(reviewRepository.findAverageRatingByCycleId(1L)).thenReturn(3.857142857);
            when(reviewRepository.findTopPerformerByCycleId(1L)).thenReturn(List.of());
            when(goalRepository.countByStatusForCycle(1L)).thenReturn(List.of());

            Responses.CycleSummary result = reviewService.getCycleSummary(1L);

            assertThat(result.getAverageRating()).isEqualTo(3.86);
        }

        @Test
        @DisplayName("returns null topPerformer when no reviews exist")
        void returnsNullTopPerformerWhenNoReviews() {
            when(cycleRepository.findById(1L)).thenReturn(Optional.of(openCycle));
            when(reviewRepository.findAverageRatingByCycleId(1L)).thenReturn(null);
            when(reviewRepository.findTopPerformerByCycleId(1L)).thenReturn(List.of());
            when(goalRepository.countByStatusForCycle(1L)).thenReturn(List.of());

            Responses.CycleSummary result = reviewService.getCycleSummary(1L);

            assertThat(result.getTopPerformer()).isNull();
            assertThat(result.getAverageRating()).isNull();
        }

        @Test
        @DisplayName("returns zero goal counts when no goals exist")
        void returnsZeroGoalCountsWhenNoGoals() {
            when(cycleRepository.findById(1L)).thenReturn(Optional.of(openCycle));
            when(reviewRepository.findAverageRatingByCycleId(1L)).thenReturn(null);
            when(reviewRepository.findTopPerformerByCycleId(1L)).thenReturn(List.of());
            when(goalRepository.countByStatusForCycle(1L)).thenReturn(List.of());

            Responses.CycleSummary result = reviewService.getCycleSummary(1L);

            assertThat(result.getCompletedGoals()).isZero();
            assertThat(result.getMissedGoals()).isZero();
        }
    }

    // ── helpers ───────────────────────────────────────────────

    private Requests.SubmitReview reviewRequest(
            Long employeeId, Long cycleId, Long reviewerId, short rating) {
        var r = new Requests.SubmitReview();
        r.setEmployeeId(employeeId);
        r.setCycleId(cycleId);
        r.setReviewerId(reviewerId);
        r.setReviewType(PerformanceReview.ReviewType.manager);
        r.setRating(rating);
        r.setCreatedBy("system");
        return r;
    }
}