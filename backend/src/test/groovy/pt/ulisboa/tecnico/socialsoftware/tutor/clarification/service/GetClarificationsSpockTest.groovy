package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class GetClarificationsSpockTest extends Specification{
    static final String NAME = "test user"
    static final String USERNAME = "test_user"
    static final Integer KEY = 1
    static final User.Role ROLE = User.Role.STUDENT
    static final String CONTENT = "I want a clarification in this question."
    static final String COURSE_NAME = "Software Architecture"
    static final int NON_EXISTING_ID = 1000

    @Autowired
    ClarificationService clarificationService

    @Autowired
    UserRepository userRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    ClarificationRepository clarificationRepository

    @Autowired
    ClarificationAnswerRepository clarificationAnswerRepository

    @Shared
    User user
    Question question
    Course course
    Clarification clarification

    def setup() {
        user = new User(NAME, USERNAME, KEY, ROLE)
        userRepository.save(user)
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        question = new Question()
        question.setKey(KEY)
        question.setCourse(course)
        questionRepository.save(question)
        course.addQuestion(question)
        clarification = new Clarification(CONTENT, question, user)
        clarificationRepository.save(clarification)
        user.addClarification(clarification)
        question.addClarification(clarification)
    }

    def "User exists and returns clarifications with no answers" () {
        when:
        def result = clarificationService.getClarificationsByUser(user.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).questionId == question.getId()
        result.get(0).userId == user.getId()
        !result.get(0).status
    }

    def "Question exists and returns clarifications with no answers" () {
        when:
        def result = clarificationService.getClarificationsByQuestion(question.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).questionId == question.getId()
        result.get(0).userId == user.getId()
        !result.get(0).status
    }

    def "Course exists and returns clarifications with no answers" () {
        when:
        def result = clarificationService.getClarificationsByCourse(course.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).questionId == question.getId()
        result.get(0).userId == user.getId()
        !result.get(0).status
    }

    def "User exists and returns clarifications with answers" () {
        given: "a clarification answer"
        def clarificationAnswer = new ClarificationAnswer(CONTENT, clarification, user)
        clarificationAnswerRepository.save(clarificationAnswer)
        clarification.addClarificationAnswer(clarificationAnswer)

        when:
        def result = clarificationService.getClarificationsByUser(user.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).questionId == question.getId()
        result.get(0).userId == user.getId()
        result.get(0).status
    }

    def "Question exists and returns clarifications with answers" () {
        given: "a clarification answer"
        def clarificationAnswer = new ClarificationAnswer(CONTENT, clarification, user)
        clarificationAnswerRepository.save(clarificationAnswer)
        clarification.addClarificationAnswer(clarificationAnswer)

        when:
        def result = clarificationService.getClarificationsByQuestion(question.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).questionId == question.getId()
        result.get(0).userId == user.getId()
        result.get(0).status
    }

    def "Course exists and returns clarifications with answers" () {
        given: "a clarification answer"
        def clarificationAnswer = new ClarificationAnswer(CONTENT, clarification, user)
        clarificationAnswerRepository.save(clarificationAnswer)
        clarification.addClarificationAnswer(clarificationAnswer)

        when:
        def result = clarificationService.getClarificationsByCourse(course.getId())

        then:
        result.get(0).content == CONTENT
        result.get(0).questionId == question.getId()
        result.get(0).userId == user.getId()
        result.get(0).status
    }

    def "User does not exists and a an error is returned" () {
        when:
        clarificationService.getClarificationsByUser(NON_EXISTING_ID)

        then:
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.USER_NOT_FOUND
    }

    def "Question does not exists and an error is returned" () {
        when:
        clarificationService.getClarificationsByQuestion(NON_EXISTING_ID)

        then:
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.QUESTION_NOT_FOUND
    }

    def "Course does not exists and an error is returned" () {
        when:
        clarificationService.getClarificationsByCourse(NON_EXISTING_ID)

        then:
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.COURSE_NOT_FOUND_ID
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {
        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
