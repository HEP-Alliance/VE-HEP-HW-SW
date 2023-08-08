// ------------------------------------------------------
//
//
//
//		RacyICs Memory Generator - RI MGene
//
//		DD/MM/YYYY Release Version
//		19/05/2016 Preliminary Release 1.0
//		25/08/2016 Software Release 1.0
//		09/09/2016 Software Release 1.0.1
//		28/09/2016 Software Release 1.0.2
//		05/10/2016 Software Release 1.0.3
//		06/10/2016 Software Release 1.0.4
//		01/12/2016 Software Release 1.0.5
//		01/12/2016 Software Release 1.0.6
//		09/02/2017 Software Release 1.0.7
//		17/02/2017 Software Release 1.0.8
//		09/03/2017 Software Release 1.0.9
//		10/08/2017 Software Release 1.0.10
//
//		Software Versions
//		RI MGene Memory Generator 1.0.10
//		RI MGene SRAM Explorer GUI 1.0
//		RI MGene Database Version 1.0.10082017
//
//		PDK Version
//		IHP SG13S rev1.0.2_a_150421
//		Generated on Tue Jun 14 11:44:35 2022		
//
// ------------------------------------------------------ 
module SRAM_1P_behavioral_bm_bist (A_ADDR,
                                A_DIN,
                                A_BM,
                                A_MEN,	// Memory enable input	-> if disabled, the memory is deactivated
                                A_WEN,	// Common write enable input (bytes maskable with BM[23:0])
                                A_REN,	// Read enable input ->  if enabled for read access when WEN=1 --> Write-through
                                A_CLK,	// Clock input
                                A_DLY,	// Delay selection signals
                                A_DOUT,

                                A_BIST_EN,
                                A_BIST_ADDR,
                                A_BIST_DIN,
                                A_BIST_BM,
                                A_BIST_MEN,
                                A_BIST_WEN,
                                A_BIST_REN,
                                A_BIST_CLK
                                );

parameter  P_DATA_WIDTH=24;
parameter  P_ADDR_WIDTH=14;

input wire  [P_ADDR_WIDTH-1:0]	A_ADDR;
input wire  [P_DATA_WIDTH-1:0] 	A_DIN;
input wire  [P_DATA_WIDTH-1:0]	A_BM;	    // write bit mask, write enabled on bit [i] if BM[i]=1'b1
input wire                      A_MEN;	// Memory enable input	-> if disabled, the memory is deactivated
input wire                      A_WEN;	// Common write enable input (bytes maskable with BM[23:0])
input wire                      A_REN;	// Read enable input ->  if enabled for read access when WEN=1 --> Write-through
input wire                      A_CLK;	// Clock input
input wire                      A_DLY;	// Delay selection signals
output wire [P_DATA_WIDTH-1:0]  A_DOUT;	// 24 Data outputs

input wire                      A_BIST_EN;
input wire  [P_ADDR_WIDTH-1:0]	A_BIST_ADDR;
input wire  [P_DATA_WIDTH-1:0] 	A_BIST_DIN;
input wire  [P_DATA_WIDTH-1:0]	A_BIST_BM;
input wire                      A_BIST_MEN;
input wire                      A_BIST_WEN;
input wire                      A_BIST_REN;
input wire                      A_BIST_CLK;




reg [P_DATA_WIDTH-1:0]    memory [0:2**(P_ADDR_WIDTH)-1]; // memory
reg [P_DATA_WIDTH-1:0]    dr_r;



wire  [P_ADDR_WIDTH-1:0]	ADDR_MUX;
wire  [P_DATA_WIDTH-1:0] 	DIN_MUX;
wire  [P_DATA_WIDTH-1:0]	BM_MUX;
wire                        MEN_MUX;
wire                        WEN_MUX;
wire                        REN_MUX;
wire                        CLK_MUX;

//BIST-MUX
assign ADDR_MUX=(A_BIST_EN==1'b1)?A_BIST_ADDR:A_ADDR;
assign DIN_MUX=(A_BIST_EN==1'b1)?A_BIST_DIN:A_DIN;
assign BM_MUX=(A_BIST_EN==1'b1)?A_BIST_BM:A_BM;
assign MEN_MUX=(A_BIST_EN==1'b1)?A_BIST_MEN:A_MEN;
assign WEN_MUX=(A_BIST_EN==1'b1)?A_BIST_WEN:A_WEN;
assign REN_MUX=(A_BIST_EN==1'b1)?A_BIST_REN:A_REN;
assign CLK_MUX=(A_BIST_EN==1'b1)?A_BIST_CLK:A_CLK;

always @(posedge CLK_MUX) begin
   if(MEN_MUX==1'b1 && WEN_MUX==1'b1) begin
		memory[ADDR_MUX] <= (memory[ADDR_MUX] & ~BM_MUX) | (DIN_MUX & BM_MUX);
		if (REN_MUX==1'b1) begin
			dr_r<= (memory[ADDR_MUX] & ~BM_MUX) | (DIN_MUX & BM_MUX);
		end
    end
    else if(MEN_MUX==1'b1 && REN_MUX==1'b1) begin
        dr_r<=memory[ADDR_MUX];
    end
end

assign A_DOUT=  dr_r;


endmodule
