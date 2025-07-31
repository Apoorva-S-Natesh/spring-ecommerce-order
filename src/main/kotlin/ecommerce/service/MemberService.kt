package ecommerce.service

import ecommerce.dto.member.RegisterRequest
import ecommerce.exception.AuthenticationException
import ecommerce.model.Member
import ecommerce.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordService: PasswordService,
    private val tokenService: TokenService,
) {
    fun register(request: RegisterRequest): String {
        if (memberRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }
        val hashedPassword = passwordService.hashPassword(request.password)
        val member =
            Member(
                email = request.email,
                password = hashedPassword,
                name = request.name,
                role = request.role,
            )
        val savedMember = memberRepository.save(member)
        return tokenService.generateToken(savedMember)
    }

    fun authenticate(
        email: String,
        password: String,
    ): String {
        val member =
            memberRepository.findByEmail(email)
                ?: throw AuthenticationException("Invalid email or password")

        if (!passwordService.verifyPassword(password, member.password)) {
            throw AuthenticationException("Invalid email or password")
        }

        return tokenService.generateToken(member)
    }
}
