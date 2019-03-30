################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../Client_Management/Client_Management.cpp 

OBJS += \
./Client_Management/Client_Management.o 

CPP_DEPS += \
./Client_Management/Client_Management.d 


# Each subdirectory must supply rules for building sources it contributes
Client_Management/%.o: ../Client_Management/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cross G++ Compiler'
	g++ -D__GXX_EXPERIMENTAL_CXX0X__ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


