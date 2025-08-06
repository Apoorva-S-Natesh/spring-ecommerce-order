package ecommerce.controller

import ecommerce.dto.member.UpdateRequest
import ecommerce.model.Member
import ecommerce.service.MemberService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/admin/member")
@RestController
class MemberController(private val memberService: MemberService) {
    @GetMapping("/{id}")
    fun getMemberById(
        @PathVariable id: Long,
    ): Member = memberService.getMemberById(id)

    @GetMapping("")
    fun getAllMembers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
    ): Page<Member> {
        return memberService.getAllMembers(page, size, sortBy)
    }

    @PutMapping("/{id}")
    fun updateMember(
        @Valid @RequestBody updateRequest: UpdateRequest,
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        memberService.updateMemberById(id, updateRequest)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}")
    fun deleteMemberById(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        memberService.deleteMemberById(id)
        return ResponseEntity.noContent().build()
    }
}
