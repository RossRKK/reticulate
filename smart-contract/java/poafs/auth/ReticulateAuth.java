package poafs.auth;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.2.0.
 */
public class ReticulateAuth extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b60008054600160a060020a033316600160a060020a03199091161790556114728061003b6000396000f3006060604052600436106100c45763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416631ffecafe81146100c9578063211f51f81461012c5780632b68b9c6146101f6578063464232cb146102095780634a632feb14610265578063638ed376146102d357806363c18a6d146103245780637a941b0314610375578063c2d7f5611461040a578063ce82eea91461045d578063de9ac36014610504578063e1d5feb1146105b4578063f1afe04d14610650575b600080fd5b34156100d457600080fd5b61012a60046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284375094965050600160a060020a03853516946020013593506106a192505050565b005b341561013757600080fd5b61017f60046024813581810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650509335935061078592505050565b60405160208082528190810183818151815260200191508051906020019080838360005b838110156101bb5780820151838201526020016101a3565b50505050905090810190601f1680156101e85780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561020157600080fd5b61012a6108a7565b341561021457600080fd5b61012a60046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284375094965050509235600160a060020a031692506108ce915050565b341561027057600080fd5b6102c160046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284375094965050509235600160a060020a03169250610aa1915050565b60405190815260200160405180910390f35b34156102de57600080fd5b61017f60046024813581810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650610b2e95505050505050565b341561032f57600080fd5b6102c160046024813581810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650610c6495505050505050565b341561038057600080fd5b61012a60046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405281815292919060208401838380828437509496505093359350610cd392505050565b341561041557600080fd5b61012a60046024813581810190830135806020601f820181900481020160405190810160405281815292919060208401838380828437509496505093359350610e0b92505050565b341561046857600080fd5b61012a60046024813581810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843782019150505050505091908035600160a060020a031690602001909190803590602001908201803590602001908080601f0160208091040260200160405190810160405281815292919060208401838380828437509496505093359350610ea092505050565b341561050f57600080fd5b6105a060046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001909190803590602001908201803590602001908080601f01602080910402602001604051908101604052818152929190602084018383808284375094965061101c95505050505050565b604051901515815260200160405180910390f35b34156105bf57600080fd5b61012a60046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001909190803590602001908201803590602001908080601f0160208091040260200160405190810160405281815292919060208401838380828437509496506111ab95505050505050565b341561065b57600080fd5b61012a60046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284375094965061125d95505050505050565b60006001846040518082805190602001908083835b602083106106d55780518252601f1990920191602091820191016106b6565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a0333166000908152600180830160205260409091200154909150600390108015906107595750600160a060020a0333166000908152600180830160205260409091200154829010155b1561077f57600160a060020a038316600090815260018083016020526040909120018290555b50505050565b61078d611355565b6001836040518082805190602001908083835b602083106107bf5780518252601f1990920191602091820191016107a0565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060020160008381526020019081526020016000208054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561089a5780601f1061086f5761010080835404028352916020019161089a565b820191906000526020600020905b81548152906001019060200180831161087d57829003601f168201915b5050505050905092915050565b60005433600160a060020a03908116911614156108cc57600054600160a060020a0316ff5b565b60006001836040518082805190602001908083835b602083106109025780518252601f1990920191602091820191016108e3565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a033316600090815260018083016020526040909120015490915060039010801590610a0057506001836040518082805190602001908083835b602083106109955780518252601f199092019160209182019101610976565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a03808416600090815260019283016020908152604080832085015433909416835285850190915290209091015410155b15610a9c576001836040518082805190602001908083835b60208310610a375780518252601f199092019160209182019101610a18565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a038316600090815260019091016020526040812090610a918282611367565b600182016000905550505b505050565b60006001836040518082805190602001908083835b60208310610ad55780518252601f199092019160209182019101610ab6565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a03831660009081526001918201602052604090200154905092915050565b610b36611355565b6001826040518082805190602001908083835b60208310610b685780518252601f199092019160209182019101610b49565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020600101600033600160a060020a0316600160a060020a031681526020019081526020016000206000018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610c585780601f10610c2d57610100808354040283529160200191610c58565b820191906000526020600020905b815481529060010190602001808311610c3b57829003601f168201915b50505050509050919050565b60006001826040518082805190602001908083835b60208310610c985780518252601f199092019160209182019101610c79565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205492915050565b602060405190810160405280828152506001846040518082805190602001908083835b60208310610d155780518252601f199092019160209182019101610cf6565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390208151905550604080519081016040528083815260200160038152506001846040518082805190602001908083835b60208310610d965780518252601f199092019160209182019101610d77565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a0333166000908152600190910160205260409020815181908051610df99291602001906113ab565b50602082015160019091015550505050565b60006001836040518082805190602001908083835b60208310610e3f5780518252601f199092019160209182019101610e20565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a033316600090815260018083016020526040909120015490915060029010610a9c575550565b60006001856040518082805190602001908083835b60208310610ed45780518252601f199092019160209182019101610eb5565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a033316600090815260018083016020526040909120015490915060039010801590610f585750600160a060020a0333166000908152600180830160205260409091200154829010155b15611015576040805190810160405280848152602001838152506001866040518082805190602001908083835b60208310610fa45780518252601f199092019160209182019101610f85565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a03861660009081526001909101602052604090208151819080516110079291602001906113ab565b506020820151600190910155505b5050505050565b60008060006001866040518082805190602001908083835b602083106110535780518252601f199092019160209182019101611034565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600086815260029182016020526040902080549093506000196101006001831615020116048451141561119d575060005b815460026000196101006001841615020190911604811015611194578381815181106110e057fe5b016020015160f860020a900460f860020a027effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff19168282815460018160011615610100020316600290048110151561113357fe5b8154600116156111525790600052602060002090602091828204019190065b9054901a60f860020a027fff00000000000000000000000000000000000000000000000000000000000000161461118c57600092506111a2565b6001016110b8565b600192506111a2565b600092505b50509392505050565b60006001846040518082805190602001908083835b602083106111df5780518252601f1990920191602091820191016111c0565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a03331660009081526001808301602052604090912001549091506002901061077f57600083815260028201602052604090208280516110159291602001906113ab565b60036001826040518082805190602001908083835b602083106112915780518252601f199092019160209182019101611272565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600160a060020a0333166000908152600191820160205260409020015410611352576001816040518082805190602001908083835b602083106113195780518252601f1990920191602091820191016112fa565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020600090555b50565b60206040519081016040526000815290565b50805460018160011615610100020316600290046000825580601f1061138d5750611352565b601f0160209004906000526020600020908101906113529190611429565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106113ec57805160ff1916838001178555611419565b82800160010185558215611419579182015b828111156114195782518255916020019190600101906113fe565b50611425929150611429565b5090565b61144391905b80821115611425576000815560010161142f565b905600a165627a7a7230582038b9b0836a912cdc73c23d2fb933eeedaad126b0b3d08c8add1136e7f680bb6f0029";

    protected ReticulateAuth(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ReticulateAuth(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<TransactionReceipt> modifyAccessLevel(String fileId, String user, BigInteger level) {
        Function function = new Function(
                "modifyAccessLevel", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.Address(user), 
                new org.web3j.abi.datatypes.generated.Uint256(level)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<byte[]> getCheckSum(String fileId, BigInteger blockIndex) {
        Function function = new Function("getCheckSum", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.generated.Uint256(blockIndex)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> destruct() {
        Function function = new Function(
                "destruct", 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> revokeShare(String fileId, String revokee) {
        Function function = new Function(
                "revokeShare", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.Address(revokee)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getAccessLevel(String fileId, String user) {
        Function function = new Function("getAccessLevel", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.Address(user)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<byte[]> getKeyForFile(String fileId) {
        Function function = new Function("getKeyForFile", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<BigInteger> getFileLength(String fileId) {
        Function function = new Function("getFileLength", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> addFile(String fileId, byte[] key, BigInteger length) {
        Function function = new Function(
                "addFile", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.DynamicBytes(key), 
                new org.web3j.abi.datatypes.generated.Uint256(length)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> updateFileLength(String fileId, BigInteger newLength) {
        Function function = new Function(
                "updateFileLength", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.generated.Uint256(newLength)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> shareFile(String fileId, String recipient, byte[] recipientKey, BigInteger level) {
        Function function = new Function(
                "shareFile", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.Address(recipient), 
                new org.web3j.abi.datatypes.DynamicBytes(recipientKey), 
                new org.web3j.abi.datatypes.generated.Uint256(level)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> compareCheckSum(String fileId, BigInteger blockIndex, byte[] checkSum) {
        Function function = new Function("compareCheckSum", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.generated.Uint256(blockIndex), 
                new org.web3j.abi.datatypes.DynamicBytes(checkSum)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> updateCheckSum(String fileId, BigInteger blockIndex, byte[] checkSum) {
        Function function = new Function(
                "updateCheckSum", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.generated.Uint256(blockIndex), 
                new org.web3j.abi.datatypes.DynamicBytes(checkSum)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> removeFile(String fileId) {
        Function function = new Function(
                "removeFile", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<ReticulateAuth> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ReticulateAuth.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<ReticulateAuth> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ReticulateAuth.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static ReticulateAuth load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ReticulateAuth(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static ReticulateAuth load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ReticulateAuth(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}