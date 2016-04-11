                    entry
samp_V_28           res		100
idx_V_30            dw		0
maxV_V_31           dw		0
minV_V_32           dw		0
util_V_33           res		230
t_V_35              dw		0
					lw		r1,t_V_35(r0)
					addi	r1,r1,0
					sw		t_V_35(r0),r1
					addi	r4,r0,21
					lw		r1,var1_V_2(r4)
					addi	r1,r1,10
					sw		var1_V_2(r0),r1
					hlt
