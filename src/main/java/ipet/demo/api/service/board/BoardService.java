package ipet.demo.api.service.board;

import ipet.demo.api.service.board.request.BoardCreateServiceRequest;
import ipet.demo.api.service.board.request.BoardUpdateServiceRequest;
import ipet.demo.api.service.board.response.BoardResponse;
import ipet.demo.domain.board.Board;
import ipet.demo.domain.board.BoardRepository;
import ipet.demo.domain.board.BoardStatus;
import ipet.demo.domain.member.Member;
import ipet.demo.exception.BusinessLogicException;
import ipet.demo.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional
    public BoardResponse createBoard(BoardCreateServiceRequest serviceRequest, Member member) {
        Board newBoard = serviceRequest.toEntity();
        newBoard.attachMember(member);
        boardRepository.save(newBoard);
        return BoardResponse.fromEntity(newBoard);
    }

    public Slice<BoardResponse> getBoards(Pageable pageable) {
        Slice<Board> boards = boardRepository.findAllByBoardStatus(BoardStatus.NORMAL, pageable);

        return boards.map(BoardResponse::fromEntity);
    }

    public BoardResponse getBoard(Long id) {
        Board board = findBoardById(id);
        return BoardResponse.fromEntity(board);
    }

    @Transactional
    public BoardResponse updateBoard(Member member, Long boardId, BoardUpdateServiceRequest serviceRequest) {
        Board board = findBoardById(boardId);
        validateWriter(member, board);
        board.updateBoard(serviceRequest.toEntity());
        return BoardResponse.fromEntity(board);
    }

    @Transactional
    public void deleteBoard(Member member, Long id, LocalDate deletedAt) {
        Board board = findBoardById(id);
        validateWriter(member, board);
        board.deleteBoard(deletedAt);
    }

    @Transactional
    public void restoreBoard(Member member, Long id) {
        Board board = findBoardById(id);
        validateWriter(member, board);
        board.restoreBoard();
    }

    private Board findBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND));
    }
    private void validateWriter(Member member, Board board) {
        if (!board.isWriter(member)) {
            throw new BusinessLogicException(ExceptionCode.BOARD_NOT_AUTHORIZED);
        }
    }
}
