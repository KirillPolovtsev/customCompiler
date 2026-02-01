.data
_newline: .asciiz "\n"
_true: .asciiz "true"
_false: .asciiz "false"
_globalVar: .word 0

.text
.globl main

main:
	addi $sp, $sp, -4
	sw $fp, 0($sp)
	move $fp, $sp
	addi $sp, $sp, -4
	sw $ra, 0($sp)
	addi $sp, $sp, -20
	li $t0, 5
	sw $t0, -12($fp)
	li $t0, 3
	sw $t0, -16($fp)
	li $t0, 30
	sw $t0, -20($fp)
	lw $t0, -12($fp)
	sw $t0, -24($fp)
	lw $t0, -16($fp)
	sw $t0, -28($fp)
	li $t0, 30
	move $a0, $t0
	li $v0, 1
	syscall
	li $t0, 10
	sw $t0, -20($fp)
	li $t0, 10
	move $a0, $t0
	li $v0, 1
	syscall
	lw $t0, -24($fp)
	move $a0, $t0
	li $v0, 1
	syscall
	lw $t0, -28($fp)
	move $a0, $t0
	li $v0, 1
	syscall
	la $a0, _newline
	li $v0, 4
	syscall
_end_main_0:
	lw $ra, -4($fp)
	move $sp, $fp
	lw $fp, 0($sp)
	addi $sp, $sp, 4
	jr $ra

add:
	addi $sp, $sp, -4
	sw $fp, 0($sp)
	move $fp, $sp
	addi $sp, $sp, -4
	sw $ra, 0($sp)
	addi $sp, $sp, -8
	lw $t0, -12($fp)
	addi $sp, $sp, -4
	sw $t0, 0($sp)
	lw $t0, -16($fp)
	move $t1, $t0
	lw $t0, 0($sp)
	addi $sp, $sp, 4
	add $t0, $t0, $t1
	move $v0, $t0
	j _end_add_1
_end_add_1:
	lw $ra, -4($fp)
	move $sp, $fp
	lw $fp, 0($sp)
	addi $sp, $sp, 4
	jr $ra

factorial:
	addi $sp, $sp, -4
	sw $fp, 0($sp)
	move $fp, $sp
	addi $sp, $sp, -4
	sw $ra, 0($sp)
	addi $sp, $sp, -8
	li $t0, 1
	sw $t0, -16($fp)
_while_3:
	lw $t0, -12($fp)
	addi $sp, $sp, -4
	sw $t0, 0($sp)
	li $t0, 1
	move $t1, $t0
	lw $t0, 0($sp)
	addi $sp, $sp, 4
	slt $t0, $t1, $t0
	beq $t0, $zero, _endwhile_4
	lw $t0, -16($fp)
	addi $sp, $sp, -4
	sw $t0, 0($sp)
	lw $t0, -12($fp)
	move $t1, $t0
	lw $t0, 0($sp)
	addi $sp, $sp, 4
	mul $t0, $t0, $t1
	sw $t0, -16($fp)
	lw $t0, -12($fp)
	addi $t0, $t0, -1
	sw $t0, -12($fp)
	j _while_3
_endwhile_4:
	lw $t0, -16($fp)
	move $v0, $t0
	j _end_factorial_2
_end_factorial_2:
	lw $ra, -4($fp)
	move $sp, $fp
	lw $fp, 0($sp)
	addi $sp, $sp, 4
	jr $ra

