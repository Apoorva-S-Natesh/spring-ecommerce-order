package ecommerce.service

import ecommerce.dto.member.MemberResponse
import ecommerce.dto.member.RegisterRequest
import ecommerce.dto.member.UpdateRequest
import ecommerce.exception.AuthenticationException
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.utils.ResponseMapper.memberToResponse
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordService: PasswordService,
    private val tokenService: TokenService,
    private val cartRepository: CartRepository,
) {
    @Transactional
    fun register(request: RegisterRequest): String {
        if (memberRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }
        val hashedPassword = passwordService.hashPassword(request.password)
        val member = request.toModel(hashedPassword)
        val newCart = Cart(member = member)
        member.assignCart(newCart)
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

    fun getMemberById(id: Long): MemberResponse {
        val member =
            memberRepository.findByIdOrNull(id)
                ?: throw NotFoundException("Member with id $id not found")
        return memberToResponse(member)
    }

    fun updateMemberById(
        id: Long,
        updateRequest: UpdateRequest,
    ) {
        val existingMember =
            memberRepository.findByIdOrNull(id)
                ?: throw NotFoundException("Member with id $id not found")
        val memberToUpdate =
            Member(
                email = updateRequest.email,
                password = existingMember.password,
                name = updateRequest.name,
                role = existingMember.role,
                cart = existingMember.cart,
                id = existingMember.id,
            )
        memberRepository.save(memberToUpdate)
    }

    fun deleteMemberById(id: Long) {
        memberRepository.findByIdOrNull(id)
            ?: throw NotFoundException("Member with id $id not found")
        memberRepository.deleteById(id)
    }

    fun getAllMembers(
        page: Int,
        size: Int,
        sortBy: String,
    ): Page<Member> {
        val pageable = PageRequest.of(page, size, Sort.by(sortBy))
        return memberRepository.findAll(pageable)
    }
}
