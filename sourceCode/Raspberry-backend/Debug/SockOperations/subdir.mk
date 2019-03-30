################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../SockOperations/Socket_Handling.cpp 

OBJS += \
./SockOperations/Socket_Handling.o 

CPP_DEPS += \
./SockOperations/Socket_Handling.d 


# Each subdirectory must supply rules for building sources it contributes
SockOperations/%.o: ../SockOperations/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cross G++ Compiler'
	g++ -D__GXX_EXPERIMENTAL_CXX0X__ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


